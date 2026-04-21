package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a row in the `lobbies` table.
 * [hostId] is the only required field at creation time; all other fields are assigned by the
 * database ([id], [lobbyCode]) or updated during the game lifecycle ([status], [winner]).
 */
@Serializable
data class Lobby(
    val id: Int? = null,
    @SerialName("lobby_code") val lobbyCode: String? = null,
    @SerialName("host_id") val hostId: String?,
    @SerialName("max_player_count") val maxPlayerCount: Int? = null,
    val status: LobbyStatus? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val winner: String? = null
)
