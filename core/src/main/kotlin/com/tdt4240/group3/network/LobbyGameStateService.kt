package com.tdt4240.group3.network

import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.network.model.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.withTimeout

object LobbyGameStateService {
    private val client = SupabaseClient.client

    suspend fun createInitialGameState(lobbyId: Int, currentPlayerId: String) {
        try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobby_gamestate").insert(
                    LobbyGameState(
                        lobbyId = lobbyId,
                        currentPlayerId = currentPlayerId,
                        turnNumber = 1
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateTurn(lobbyId: Int, nextPlayerId: String, turnNumber: Int) {
        try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobby_gamestate").update({
                    set("current_player_id", nextPlayerId)
                    set("turn_number", turnNumber)
                }) {
                    filter {
                        eq("lobby_id", lobbyId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setLobbyMapStates(lobbyMapStates: List<LobbyMapState>) {
        try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobby_map_state").upsert(lobbyMapStates)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
