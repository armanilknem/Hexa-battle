package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.*
import io.github.jan.supabase.postgrest.from

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

    suspend fun moveTroop(troopId: Int, q: Int, r: Int) {
        client.from("lobby_troops").update({
            set("q", q)
            set("r", r)
        }) {
            filter { eq("id", troopId) }
        }
    }

    suspend fun insertTroops(lobbyId: Int, troops: List<LobbyTroop>) {
        if (troops.isNotEmpty()) {
            client.from("lobby_troops").insert(troops)
        }
    }

    suspend fun insertTiles(lobbyId: Int, tiles: List<LobbyTile>) {
        if (tiles.isNotEmpty()) {
            client.from("lobby_tiles").insert(tiles)
        }
    }

    suspend fun insertCities(lobbyId: Int, cities: List<LobbyCity>) {
        if (cities.isNotEmpty()) {
            client.from("lobby_cities").insert(cities)
        }
    }
}
