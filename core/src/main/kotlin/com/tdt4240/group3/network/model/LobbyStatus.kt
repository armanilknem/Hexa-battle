package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LobbyStatus {
    @SerialName("open") OPEN,
    @SerialName("closed") CLOSED,
    @SerialName("playing") PLAYING,
    @SerialName("finished") FINISHED
}
