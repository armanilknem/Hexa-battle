package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a row in the `lobby_gamestate` table.
 * Tracks whose turn it is and the current turn number for an active game.
 * [mapReady] is set to `true` by the host once the initial map state has been uploaded.
 */
@Serializable
data class LobbyGameState(
    @SerialName("lobby_id") val lobbyId: Int,
    @SerialName("current_player_id") val currentPlayerId: String? = null,
    @SerialName("turn_number") val turnNumber: Int = 1,
    @SerialName("map_ready") val mapReady: Boolean? = false,
    @SerialName("updated_at") val updatedAt: String? = null
)
