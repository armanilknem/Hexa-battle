package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Lifecycle state of a lobby. Maps to the `status` column in the `lobbies` table.
 *
 * - [OPEN]      — lobby is visible and accepting players
 * - [CLOSED]    — lobby is no longer accepting new players
 * - [PLAYING]   — the game is in progress
 * - [FINISHED]  — the game has ended with a declared winner
 * - [ABANDONED] — the game was terminated without a winner
 */
@Serializable
enum class LobbyStatus {
    @SerialName("open") OPEN,
    @SerialName("closed") CLOSED,
    @SerialName("playing") PLAYING,
    @SerialName("finished") FINISHED,
    @SerialName("abandoned") ABANDONED
}
