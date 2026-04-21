package com.tdt4240.group3.network

import com.badlogic.gdx.Gdx
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.network.model.LobbyGameState
import com.tdt4240.group3.network.model.LobbyMapState
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.withTimeout

/** Manages reads and writes to the `lobby_gamestate` and `lobby_map_state` tables. */
object LobbyGameStateService {
    private const val TAG = "LobbyGameStateService"
    private val client = SupabaseClient.client

    /** Inserts the initial game-state row for [lobbyId], designating [currentPlayerId] as first to move. */
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
            Gdx.app.error(TAG, "Failed to create initial game state for lobby $lobbyId", e)
        }
    }

    /** Advances the turn for [lobbyId]: sets the active player to [nextPlayerId] and increments [turnNumber]. */
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
            Gdx.app.error(TAG, "Failed to update turn for lobby $lobbyId", e)
        }
    }

    /** Upserts [lobbyMapStates] into `lobby_map_state`, synchronising the full map snapshot for a lobby. */
    suspend fun setLobbyMapStates(lobbyMapStates: List<LobbyMapState>) {
        try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobby_map_state").upsert(lobbyMapStates)
            }
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Failed to upsert ${lobbyMapStates.size} map states", e)
        }
    }
}
