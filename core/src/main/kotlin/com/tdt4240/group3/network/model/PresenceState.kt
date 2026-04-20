package com.tdt4240.group3.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PresenceState(
    val playerId: String,
    val playerName: String
)
