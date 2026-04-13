package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyPlayer
import com.tdt4240.group3.network.model.LobbyStatus
import io.github.jan.supabase.postgrest.postgrest

object LobbyService {
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun createLobby(hostId: String): LobbyResult {
        return try {
            PlayerService.getOrCreatePlayer(hostId)

            val inserted = postgrest["lobbies"].insert(Lobby(hostId = hostId)) {
                select()
            }.decodeSingle<Lobby>()

            val newLobby = postgrest["lobbies"].select {
                filter { eq("id", inserted.id!!) }
            }.decodeSingle<Lobby>()

            LobbyResult.Success(newLobby)
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Failed to create lobby")
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

            if ((lobby.playerCount ?: 0) >= (lobby.maxPlayerCount ?: 2)) {
                return LobbyResult.Error("Lobby full")
            }

            postgrest["lobby_players"].insert(LobbyPlayer(lobbyId = lobby.id!!, playerId = playerId))

            val updatedLobby = postgrest["lobbies"].select {
                filter { eq("id", lobby.id!!) }
            }.decodeSingle<Lobby>()

            LobbyResult.Success(updatedLobby)
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Error joining lobby")
        }
    }

    suspend fun updateStatus(lobbyId: Int, status: LobbyStatus) {
        postgrest["lobbies"].update({
            set("status", status)
        }) {
            filter {
                eq("id", lobbyId)
            }
        }
    }
}

sealed class LobbyResult {
    data class Success(val lobby: Lobby) : LobbyResult()
    data class Error(val message: String) : LobbyResult()
}
