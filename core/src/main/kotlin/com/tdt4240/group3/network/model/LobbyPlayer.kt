package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Join-table record linking a player to a lobby. Maps to the `lobby_players` table. */
@Serializable
data class LobbyPlayer(
    @SerialName("lobby_id") val lobbyId: Int,
    @SerialName("player_id") val playerId: String
)
