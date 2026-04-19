package com.tdt4240.group3.network

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.ecs.components.GameStateComponent
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.view.screens.PlayScreen
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.postgresListDataFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ktx.ashley.allOf
import ktx.ashley.get

class MultiplayerManager(
    private val lobbyId: Int,
    private val myPlayerId: String,
    private val engine: Engine,
    private val screen: PlayScreen
) {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        val channel = client.channel("lobby_gamestate_${lobbyId}")
        scope.launch {
            val gamestateFlow = channel.postgresSingleDataFlow(schema="public", table="lobby_gamestate", primaryKey=LobbyGameState::lobbyId) {
                eq("lobby_id", lobbyId)
            }
            gamestateFlow.onEach { updatedGamestate ->
                val gameStateEntity = engine.getEntitiesFor(
                    allOf(GameStateComponent::class).get()
                ).firstOrNull()
                val gs = gameStateEntity?.get(GameStateComponent.mapper)

                gs?.turnCount = updatedGamestate.turnNumber
                gs?.currentPlayerIndex = gs.playerOrder.indexOf(updatedGamestate.currentPlayerId!!)
            }.launchIn(scope)

            val mapstateFlow = channel.postgresChangeFlow<PostgresAction>(schema="public") {
                table = "lobby_map_state"
                filter("lobby_id", FilterOperator.EQ, lobbyId)
            }
            mapstateFlow.onEach {
                when(it) {
                    is PostgresAction.Delete -> println("Deleted: ${it.oldRecord}")
                    is PostgresAction.Insert -> println("Inserted: ${it.record}")
                    is PostgresAction.Select -> println("Selected: ${it.record}")
                    is PostgresAction.Update -> println("Updated: ${it.oldRecord} with ${it.record}")
                }
            }.launchIn(scope)

            channel.subscribe()
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
