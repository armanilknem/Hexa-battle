package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.entities.EntityFactory
import com.tdt4240.group3.model.team.TeamName
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
    private val entityFactory: EntityFactory
) {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
                val gameStateEntity = engine.getEntitiesFor(
                    allOf(GameStateComponent::class).get()
                ).firstOrNull()

                val gs = gameStateEntity?.get(GameStateComponent.mapper)
                if (gs != null) {
                    gs.turnCount = updated.turnNumber
                    gs.currentPlayerIndex = gs.playerOrder.indexOf(updated.currentPlayerId!!)
                }
            }.launchIn(scope)

            val mapstateFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "lobby_map_state"
                filter("lobby_id", FilterOperator.EQ, lobbyId)
            }
            mapstateFlow.onEach { action ->
                when (action) {
                    is PostgresAction.Delete, // Doesn't happen
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

                        val gsEntity = engine.getEntitiesFor(
                            allOf(GameStateComponent::class).get()
                        ).firstOrNull()
                        val gs = gsEntity?.get(GameStateComponent.mapper)

                        val teamName: TeamName =
                            if (ownerId != null && gs != null) {
                                val idx = gs.playerOrder.indexOf(ownerId)
                                if (idx >= 0 && idx < gs.activeTeams.size) gs.activeTeams[idx] else TeamName.NONE
                            } else TeamName.NONE

                        // Update troop first
                        val troopsAtTile = mutableListOf<Entity>()
                        val entities = engine.entities
                        val size = entities.size()

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
                            entityFactory.createTroop(
                                team = teamName,
                                unitKey = "baseTroop",
                                strength = strength,
                                q = q,
                                r = r
                            )
                        }

                        // Then city
                        val citiesAtTile = mutableListOf<Entity>()
                        for (i in 0 until size) {
                            val entity = entities[i]
                            val pos = entity[PositionComponent.mapper] ?: continue
                            if (pos.q == q && pos.r == r && entity[CityComponent.mapper] != null) {
                                citiesAtTile.add(entity)
                            }
                        }

                        for (entity in citiesAtTile) {
                            entity[TeamComponent.mapper]?.team = teamName
                        }

                        // Then tile
                        val tilesAtTile = mutableListOf<Entity>()
                        for (i in 0 until size) {
                            val entity = entities[i]
                            val pos = entity[PositionComponent.mapper] ?: continue
                            if (pos.q == q && pos.r == r && entity[TileComponent.mapper] != null) {
                                tilesAtTile.add(entity)
                            }
                        }

                        for (entity in tilesAtTile) {
                            entity[TeamComponent.mapper]?.team = teamName
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
