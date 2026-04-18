package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LobbyCity(
    val id: Int? = null,
    @SerialName("lobby_id") val lobbyId: Int,
    val q: Int,
    val r: Int,
    @SerialName("owner_id") val ownerId: String? = null,
    val name: String
)
