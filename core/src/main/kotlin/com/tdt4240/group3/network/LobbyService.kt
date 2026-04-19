package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyPlayer
import com.tdt4240.group3.network.model.LobbyResult
import io.github.jan.supabase.postgrest.from

object LobbyService {
    private val client = SupabaseClient.client

    suspend fun getOrCreateLobby(hostId: String): LobbyResult {
        return try {
            PlayerService.getOrCreatePlayer(hostId)

            val existing = client.from("lobbies").select {
                filter {
                    eq("host_id", hostId)
                    eq("status", "open")
                }
            }.decodeSingleOrNull<Lobby>()
            if (existing != null) return LobbyResult.Success(existing)

            val inserted = client.from("lobbies").insert(Lobby(hostId = hostId)) {
                select()
            }.decodeSingle<Lobby>()
            LobbyResult.Success(inserted)
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Failed to get or create lobby: ${e.message}")
        }
    }

    suspend fun getLobbyPlayers(lobbyId: Int): List<LobbyPlayer> {
        return try {
            val lobbyPlayers = client.from("lobby_players").select() {
                filter {
                    eq("lobby_id", lobbyId)
                }
            }.decodeList<LobbyPlayer>()
            lobbyPlayers
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun joinLobbyByCode(code: String, playerId: String): LobbyResult {
        return try {
            PlayerService.getOrCreatePlayer(playerId)

            val lobby = client.from("lobbies").select {
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

    suspend fun startGame(lobbyId: Int, playerIds: List<String>): Boolean {
        return try {
            val players = playerIds.map { playerId ->
                LobbyPlayer(lobbyId = lobbyId, playerId = playerId)
            }
            client.from("lobby_players").insert(players)

            client.from("lobbies").update({
                set("status", "playing")
            }) {
                filter {
                    eq("id", lobbyId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
