package com.tdt4240.group3.network.model

import kotlinx.serialization.Serializable

/**
 * Payload broadcast via Supabase Realtime Presence to advertise a connected player inside
 * a lobby channel. Each client tracks all currently-online peers via this state.
 */
@Serializable
data class PresenceState(
    val playerId: String,
    val playerName: String
)
