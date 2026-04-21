package com.tdt4240.group3.network.model

/**
 * Discriminated union returned by lobby operations.
 * [Success] carries the [Lobby] that was found or created; [Error] carries a human-readable message.
 */
sealed class LobbyResult {
    data class Success(val lobby: Lobby) : LobbyResult()
    data class Error(val message: String) : LobbyResult()
}
