package com.tdt4240.group3.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LobbyMapState(
    @SerialName("lobby_id") val lobbyId: Int,
    val q: Int,
    val r: Int,
    @SerialName("owner_id") val ownerId: String? = null,
    val strength: Int? = null
)
