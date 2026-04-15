package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LobbyPlayer(
    @SerialName("lobby_id") val lobbyId: Int,
    @SerialName("player_id") val playerId: String
)
