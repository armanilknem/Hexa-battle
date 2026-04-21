package com.tdt4240.group3.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * DTO for a row in the `lobby_map_state` table.
 * Represents one hex cell: its axial coordinates ([q], [r]), which player owns it ([ownerId]),
 * and the troop [strength] on that cell (`null` if unoccupied).
 */
@Serializable
data class LobbyMapState(
    @SerialName("lobby_id") val lobbyId: Int,
    val q: Int,
    val r: Int,
    @SerialName("owner_id") val ownerId: String? = null,
    val strength: Int? = null
)
