package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.UnitType
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.entities.TroopConfig
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.network.model.LobbyMapState
import com.tdt4240.group3.view.screens.PlayScreen
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

                // First snapshot (turn 1): enable syncing, but DO NOT touch
                // the locally initialized GameStateComponent.
                if (lastSeenTurn == 0 && newTurn == 1) {
                    lastSeenTurn = 1
                    mapStateReady = true
                    val isMyTurn = updated.currentPlayerId == myPlayerId
                    Gdx.app.postRunnable { screen.onTurnChanged(isMyTurn) }
                    return@onEach
                }

                Gdx.app.postRunnable {
                    val gameStateEntity = engine.getEntitiesFor(
                        allOf(GameStateComponent::class).get()
                    ).firstOrNull()

                    val gs = gameStateEntity?.get(GameStateComponent.mapper)
                    if (gs != null) {
                        gs.turnCount = newTurn

                        val currentId = updated.currentPlayerId
                        if (currentId != null) {
                            val idx = gs.playerOrder.indexOf(currentId)
                            if (idx >= 0) {
                                gs.currentPlayerIndex = idx
                            }
                        }
                        screen.onTurnChanged(updated.currentPlayerId == myPlayerId)
                    }
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
                                troopFactory.createEntity(TroopConfig(
                                    team = teamName,
                                    unitType = UnitType.SOLDIER,
                                    strength = strength,
                                    q = q,
                                    r = r
                                ))
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
