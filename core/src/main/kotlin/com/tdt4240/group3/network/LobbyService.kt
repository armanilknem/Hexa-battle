package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyPlayer
import com.tdt4240.group3.network.model.LobbyResult
import com.tdt4240.group3.network.model.LobbyStatus
import io.github.jan.supabase.postgrest.postgrest

object LobbyService {
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getOrCreateLobby(hostId: String): LobbyResult {
        return try {
            PlayerService.getOrCreatePlayer(hostId)

            val existing = postgrest["lobbies"].select {
                filter {
                    eq("host_id", hostId)
                    eq("status", "open")
                }
            }.decodeSingleOrNull<Lobby>()
            if (existing != null) return LobbyResult.Success(existing)

            val inserted = postgrest["lobbies"].insert(Lobby(hostId = hostId)) {
                select()
            }.decodeSingle<Lobby>()
            LobbyResult.Success(inserted)
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Failed to get or create lobby: ${e.message}")
        }
    }

    suspend fun joinLobbyByCode(code: String, playerId: String): LobbyResult {
        return try {
            PlayerService.getOrCreatePlayer(playerId)

            val lobby = postgrest["lobbies"].select {
                filter {
                    eq("lobby_code", code)
                    eq("status", "open")
                }
            }.decodeSingleOrNull<Lobby>() ?: return LobbyResult.Error("Lobby not found")

            LobbyResult.Success(lobby)
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Error joining lobby: ${e.message}")
        }
    }

    suspend fun closeLobby(lobbyId: Int): Boolean {
        return try {
            postgrest["lobbies"].update({
                set("status", LobbyStatus.CLOSED)
            }) {
                filter { eq("id", lobbyId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun startGame(lobbyId: Int, playerIds: List<String>): Boolean {
        return try {
            // Add all players to lobby_players table when game starts
            playerIds.forEach { playerId ->
                postgrest["lobby_players"].insert(
                    LobbyPlayer(lobbyId = lobbyId, playerId = playerId)
                )
            }
            postgrest["lobbies"].update({
                set("status", "playing")
            }) {
                filter { eq("id", lobbyId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
