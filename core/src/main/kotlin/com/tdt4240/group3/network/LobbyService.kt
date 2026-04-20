package com.tdt4240.group3.network

import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.network.model.Lobby
import com.tdt4240.group3.network.model.LobbyPlayer
import com.tdt4240.group3.network.model.LobbyResult
import com.tdt4240.group3.network.model.Player
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.withTimeout

object LobbyService {
    private val client = SupabaseClient.client

    suspend fun getOrCreateLobby(hostId: String): LobbyResult {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                PlayerService.getOrCreatePlayer(hostId)

                val existing = client.from("lobbies").select {
                    filter {
                        eq("host_id", hostId)
                        eq("status", "open")
                    }
                }.decodeSingleOrNull<Lobby>()
                if (existing != null) return@withTimeout LobbyResult.Success(existing)

                val inserted = client.from("lobbies").insert(Lobby(hostId = hostId)) {
                    select()
                }.decodeSingle<Lobby>()
                LobbyResult.Success(inserted)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Failed to get or create lobby: ${e.message}")
        }
    }

    suspend fun getLobbyPlayers(lobbyId: Int): List<LobbyPlayer> {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobby_players").select() {
                    filter {
                        eq("lobby_id", lobbyId)
                    }
                }.decodeList<LobbyPlayer>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun joinLobbyByCode(code: String, playerId: String): LobbyResult {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                PlayerService.getOrCreatePlayer(playerId)

                val lobby = client.from("lobbies").select {
                    filter {
                        eq("lobby_code", code)
                        eq("status", "open")
                    }
                }.decodeSingleOrNull<Lobby>() ?: return@withTimeout LobbyResult.Error("Lobby not found")

                LobbyResult.Success(lobby)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LobbyResult.Error("Error joining lobby: ${e.message}")
        }
    }

    suspend fun startGame(lobbyId: Int, playerIds: List<String>): Boolean {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun endGame(lobbyId: Int, winnerId: String) {
        try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("lobbies").update({
                    set("status", "finished")
                    set("winner", winnerId)
                }) {
                    filter {
                        eq("id", lobbyId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTopWinners(limit: Int = 5): List<Pair<String, Int>> {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                val winnerLobbies = client.from("lobbies").select {
                    filter {
                        filterNot("winner", FilterOperator.IS, null)
                    }
                }.decodeList<Lobby>()

                val winCounts = winnerLobbies
                    .mapNotNull { it.winner }
                    .groupingBy { it }
                    .eachCount()

                if (winCounts.isEmpty()) return@withTimeout emptyList()

                val topIds = winCounts.entries
                    .sortedByDescending { it.value }
                    .take(limit)
                    .map { it.key }

                val players = client.from("players").select {
                    filter { isIn("id", topIds) }
                }.decodeList<Player>()

                val nameById = players.associate { it.id to it.displayName }

                topIds.map { id ->
                    val name = nameById[id] ?: "Unknown"
                    val wins = winCounts[id] ?: 0
                    name to wins
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
