package com.tdt4240.group3.network.model

sealed class LobbyResult {
    data class Success(val lobby: Lobby) : LobbyResult()
    data class Error(val message: String) : LobbyResult()
}
