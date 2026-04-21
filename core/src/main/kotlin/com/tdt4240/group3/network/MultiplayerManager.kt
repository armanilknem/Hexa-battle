package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.SelectableComponent
import com.tdt4240.group3.model.entities.TroopConfig
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.network.model.LobbyMapState
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Manages Supabase Realtime subscriptions for an active multiplayer game.
 * Listens for changes to [lobbyId]'s game-state and map-state rows and applies them
 * to the Ashley [engine] on the LibGDX main thread.
 *
 * Call [start] once after the lobby transitions to `playing`, and [dispose] when the
 * screen is torn down to cancel all coroutines.
 */
class MultiplayerManager(
    private val lobbyId:       Int,
    private val myPlayerId:    String,
    private val engine:        Engine,
    private val onTurnChanged: (Boolean) -> Unit,
    private val troopFactory:  TroopFactory
) {
    private val client = SupabaseClient.client
    private val scope  = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lastSeenTurn:     Int     = 0
    private var lastSeenPlayerId: String? = null
    private var mapStateReady:    Boolean = false

    // Pre-built entity families for efficient filtered queries.
    private val gsFamily    = allOf(GameStateComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()

    fun start() {
        val channel = client.channel("lobby_gamestate_$lobbyId")

        scope.launch {
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
                    }
                    onTurnChanged(newPlayerId == myPlayerId)
                }
            }.launchIn(scope)

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
                            engine.getSystem(TurnSystem::class.java)?.resetActivityTimer()

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

            channel.subscribe()
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
