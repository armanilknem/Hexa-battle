package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.SelectableComponent
import com.tdt4240.group3.model.entities.TroopConfig
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.network.model.LobbyMapState
import com.tdt4240.group3.network.model.PresenceState
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.presenceDataFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages Supabase Realtime subscriptions for an active multiplayer game.
 *
 * Tracks:
 * - Game state (turn advances) via [postgresSingleDataFlow]
 * - Map state (troop/city/tile ownership) via [postgresChangeFlow]
 * - Player presence (connection status + display names) via [presenceDataFlow]
 *
 * [onPresenceChanged] receives both the set of connected player IDs and a map of
 * id -> display name so the UI can show real names without a separate DB lookup.
 */
class MultiplayerManager(
    private val lobbyId:           Int,
    private val myPlayerId:        String,
    private val myPlayerName:      String,
    private val engine:            Engine,
    private val onTurnChanged:     (Boolean) -> Unit,
    private val onPresenceChanged: (connectedIds: Set<String>, names: Map<String, String>) -> Unit,
    private val troopFactory:      TroopFactory
) {
    private val client = SupabaseClient.client
    private val scope  = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lastSeenTurn:     Int     = 0
    private var lastSeenPlayerId: String? = null
    private var mapStateReady:    Boolean = false

    /**
     * Counts how many times the local player's own turn was ended remotely (AFK strike).
     * When this reaches [GameConstants.INACTIVITY_STRIKE_LIMIT] the local player is
     * added to [GameStateComponent.eliminatedTeams], mirroring what [TurnSystem] already
     * does on the watching player's machine and ensuring [com.tdt4240.group3.model.systems.WinSystem] fires on both clients.
     */
    private var selfStrikeCount:  Int     = 0

    // Pre-built entity families for efficient filtered queries.
    private val gsFamily    = allOf(GameStateComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()

    fun start() {
        val channel = client.channel("lobby_gamestate_$lobbyId")

        scope.launch {
            // Presence tracking
            val presenceFlow = channel.presenceDataFlow<PresenceState>()
            presenceFlow.onEach { presenceList ->
                val connectedIds = presenceList.map { it.playerId }.toSet()
                val names        = presenceList.associate { it.playerId to it.playerName }
                Gdx.app.postRunnable {
                    onPresenceChanged(connectedIds, names)
                }
            }.launchIn(scope)

            // Game state tracking
            val gamestateFlow = channel.postgresSingleDataFlow(
                schema = "public",
                table = "lobby_gamestate",
                primaryKey = LobbyGameState::lobbyId
            ) {
                eq("lobby_id", lobbyId)
            }

            gamestateFlow.onEach { updated ->
                val newTurn     = updated.turnNumber
                val newPlayerId = updated.currentPlayerId

                // Deduplicate: skip if we already processed this exact turn+player state.
                if (newTurn == lastSeenTurn && newPlayerId == lastSeenPlayerId) return@onEach
                lastSeenTurn     = newTurn
                lastSeenPlayerId = newPlayerId

                if (!mapStateReady) mapStateReady = true

                // Apply the new turn state on the main thread, then trigger troop spawning
                // so TroopCreationSystem sets movesLeft and marks selectable troops correctly.
                Gdx.app.postRunnable {
                    val gs = engine.getEntitiesFor(gsFamily)
                        .firstOrNull()
                        ?.get(GameStateComponent.mapper)
                    if (gs != null) {
                        // Sample BEFORE updating gs: detect whether it was the local player's
                        // turn that just ended. When endTurn() fires locally, gs.currentPlayerIndex
                        // is already advanced before this postRunnable executes, so wasMyTurn
                        // will be false in that case — no false positive.
                        val wasMyTurn = gs.playerOrder.getOrNull(gs.currentPlayerIndex) == myPlayerId

                        gs.turnCount = newTurn
                        if (newPlayerId != null) {
                            val idx = gs.playerOrder.indexOf(newPlayerId)
                            if (idx >= 0) gs.currentPlayerIndex = idx
                        }
                        // Mark selectable directly — do NOT add NeedsTroopSpawnComponent here.
                        // createTroopsForTeam runs only via endTurn() on the machine whose
                        // turn just ended; running it here too would double-reinforce every city.
                        engine.getSystem(TroopCreationSystem::class.java)?.markSelectable(gs)
                        engine.getSystem(TurnSystem::class.java)?.onRemoteTurnStarted()

                        // AFK self-detection: if it was my turn and the turn changed to someone
                        // else remotely, the other player's TurnSystem fired an AFK strike against
                        // me. Accumulate strikes locally so WinSystem fires on this machine too.
                        if (wasMyTurn && newPlayerId != myPlayerId) {
                            val myIndex = gs.playerOrder.indexOf(myPlayerId)
                            val myTeam  = gs.activeTeams.getOrNull(myIndex)
                            if (myTeam != null && myTeam !in gs.eliminatedTeams) {
                                selfStrikeCount++
                                if (selfStrikeCount >= GameConstants.INACTIVITY_STRIKE_LIMIT) {
                                    gs.eliminatedTeams.add(myTeam)
                                    selfStrikeCount = 0
                                }
                            }
                        }
                    }
                    onTurnChanged(newPlayerId == myPlayerId)
                }
            }.launchIn(scope)

            // Map state tracking
            val mapstateFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "lobby_map_state"
                filter("lobby_id", FilterOperator.EQ, lobbyId)
            }

            mapstateFlow.onEach { action ->
                // Only start applying map state once we've seen at least the
                // first gamestate snapshot.
                if (!mapStateReady) return@onEach

                when (action) {
                    is PostgresAction.Delete,
                    is PostgresAction.Select -> {}

                    is PostgresAction.Insert,
                    is PostgresAction.Update -> {
                        val mapState = Json.decodeFromJsonElement(
                            LobbyMapState.serializer(),
                            action.record
                        )

                        val q        = mapState.q
                        val r        = mapState.r
                        val strength = mapState.strength ?: 0
                        val ownerId  = mapState.ownerId

                        // All engine mutations must happen on the main thread.
                        Gdx.app.postRunnable {
                            // Only reset the AFK timer when the map change belongs to the
                            // current player. Passing ownerId lets TurnSystem discard stale
                            // events that arrive after the turn has already advanced.
                            if (ownerId != null) {
                                engine.getSystem(TurnSystem::class.java)?.resetActivityTimer(ownerId)
                                // Mirror the watcher-side strike reset: if I made a move,
                                // clear my own accumulated AFK-strike counter too.
                                if (ownerId == myPlayerId) selfStrikeCount = 0
                            }

                            val gs = engine.getEntitiesFor(gsFamily)
                                .firstOrNull()
                                ?.get(GameStateComponent.mapper)

                            val team: Team =
                                if (ownerId != null && gs != null) {
                                    val idx = gs.playerOrder.indexOf(ownerId)
                                    if (idx >= 0 && idx < gs.activeTeams.size) gs.activeTeams[idx]
                                    else Team.NONE
                                } else Team.NONE

                            applyTroopUpdate(q, r, strength, team, gs)
                            applyCityUpdate(q, r, team)
                            applyTileUpdate(q, r, team)
                        }
                    }
                }
            }.launchIn(scope)

            channel.subscribe(blockUntilSubscribed = true)

            channel.track(
                Json.encodeToJsonElement(
                    PresenceState(myPlayerId, myPlayerName)
                ).jsonObject
            )
        }
    }

    private fun applyTroopUpdate(
        q:        Int,
        r:        Int,
        strength: Int,
        team:     Team,
        gs:       GameStateComponent?
    ) {
        val troopEntity = engine.getEntitiesFor(troopFamily).firstOrNull { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }

        if (troopEntity != null) {
            val troop = troopEntity[TroopComponent.mapper]!!
            if (strength > 0) {
                troop.strength = strength
                troopEntity[TeamComponent.mapper]?.team = team
            } else {
                troopEntity.remove(TroopComponent::class.java)
            }
        } else if (strength > 0) {
            val newTroop = troopFactory.createEntity(TroopConfig(
                team     = team,
                strength = strength,
                q        = q,
                r        = r
            ))
            if (gs != null && team == gs.currentTeam) {
                newTroop.add(SelectableComponent())
            }
        }
    }

    private fun applyCityUpdate(q: Int, r: Int, team: Team) {
        engine.getEntitiesFor(cityFamily).firstOrNull { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }?.get(TeamComponent.mapper)?.team = team
    }

    private fun applyTileUpdate(q: Int, r: Int, team: Team) {
        engine.getEntitiesFor(tileFamily).firstOrNull { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }?.get(TeamComponent.mapper)?.team = team
    }

    fun dispose() {
        scope.cancel()
    }
}
