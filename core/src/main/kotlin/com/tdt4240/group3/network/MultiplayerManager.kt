package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.entities.TroopConfig
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.network.model.LobbyMapState
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.model.components.marker.SelectableComponent
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TurnSystem
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

class MultiplayerManager(
    private val lobbyId: Int,
    private val myPlayerId: String,
    private val engine: Engine,
    private val screen: PlayScreen,
    private val troopFactory: TroopFactory
) {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lastSeenTurn: Int = 0
    private var lastSeenPlayerId: String? = null

    private var mapStateReady: Boolean = false

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
                val newTurn = updated.turnNumber
                val newPlayerId = updated.currentPlayerId

                // Deduplicate: skip if we already processed this exact turn+player state.
                if (newTurn == lastSeenTurn && newPlayerId == lastSeenPlayerId) return@onEach
                lastSeenTurn = newTurn
                lastSeenPlayerId = newPlayerId

                if (!mapStateReady) mapStateReady = true

                // Apply the new turn state on the main thread, then trigger troop spawning
                // so TroopCreationSystem sets movesLeft and marks selectable troops correctly.
                Gdx.app.postRunnable {
                    val gs = engine.getEntitiesFor(
                        allOf(GameStateComponent::class).get()
                    ).firstOrNull()?.get(GameStateComponent.mapper)
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
                    screen.onTurnChanged(newPlayerId == myPlayerId)
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

                        val q = mapState.q
                        val r = mapState.r
                        val strength = mapState.strength ?: 0
                        val ownerId = mapState.ownerId

                        // Again: all engine work on main thread.
                        Gdx.app.postRunnable {
                            engine.getSystem(TurnSystem::class.java)?.resetActivityTimer()

                            val gsEntity = engine.getEntitiesFor(
                                allOf(GameStateComponent::class).get()
                            ).firstOrNull()
                            val gs = gsEntity?.get(GameStateComponent.mapper)

                            val teamName: Team =
                                if (ownerId != null && gs != null) {
                                    val idx = gs.playerOrder.indexOf(ownerId)
                                    if (idx >= 0 && idx < gs.activeTeams.size) gs.activeTeams[idx]
                                    else Team.NONE
                                } else Team.NONE

                            val entities = engine.entities
                            val size = entities.size()

                            // Update troop
                            val troopsAtTile = mutableListOf<Entity>()
                            for (i in 0 until size) {
                                val entity = entities[i]
                                val pos = entity[PositionComponent.mapper] ?: continue
                                if (pos.q == q && pos.r == r && entity[TroopComponent.mapper] != null) {
                                    troopsAtTile.add(entity)
                                }
                            }

                            if (troopsAtTile.isNotEmpty()) {
                                val troopEntity = troopsAtTile.first()
                                val troop = troopEntity[TroopComponent.mapper]!!
                                if (strength > 0) {
                                    troop.strength = strength
                                    troopEntity[TeamComponent.mapper]?.team = teamName
                                } else {
                                    troopEntity.remove(TroopComponent::class.java)
                                }
                            } else if (strength > 0) {
                                val newTroop = troopFactory.createEntity(TroopConfig(
                                    team = teamName,
                                    strength = strength,
                                    q = q,
                                    r = r
                                ))
                                if (gs != null && teamName == gs.currentTeam) {
                                    newTroop.add(engine.createComponent(SelectableComponent::class.java))
                                }
                            }

                            // Update city
                            for (i in 0 until size) {
                                val entity = entities[i]
                                val pos = entity[PositionComponent.mapper] ?: continue
                                if (pos.q == q && pos.r == r && entity[CityComponent.mapper] != null) {
                                    entity[TeamComponent.mapper]?.team = teamName
                                }
                            }

                            // Update tile
                            for (i in 0 until size) {
                                val entity = entities[i]
                                val pos = entity[PositionComponent.mapper] ?: continue
                                if (pos.q == q && pos.r == r && entity[TileComponent.mapper] != null) {
                                    entity[TeamComponent.mapper]?.team = teamName
                                }
                            }
                        }
                    }
                }
            }.launchIn(scope)

            channel.subscribe()
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
