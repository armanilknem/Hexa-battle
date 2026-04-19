package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.*
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

object LobbyGameStateService {
    private val client = SupabaseClient.client

    suspend fun createInitialGameState(lobbyId: Int, currentPlayerId: String) {
        client.from("lobby_gamestate").insert(
            LobbyGameState(
                lobbyId = lobbyId,
                currentPlayerId = currentPlayerId,
                turnNumber = 1
            )
        )
    }

    suspend fun updateTurn(lobbyId: Int, nextPlayerId: String, turnNumber: Int) {
        client.from("lobby_gamestate").update({
            set("current_player_id", nextPlayerId)
            set("turn_number", turnNumber)
        }) {
            filter {
                eq("lobby_id", lobbyId)
            }
        }
    }

    suspend fun setLobbyMapStates(lobbyMapStates: List<LobbyMapState>) {
        println("Sending to Supabase: " + Json.encodeToString(lobbyMapStates))

        client.from("lobby_map_state").upsert(lobbyMapStates)
    }
}
