package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LobbyTroop(
    val id: Int? = null,
    @SerialName("lobby_id") val lobbyId: Int,
    @SerialName("owner_id") val ownerId: String,
    val q: Int,
    val r: Int,
    val strength: Int,
    @SerialName("has_moved") val hasMoved: Boolean? = null
)
