package com.tdt4240.group3.config

object GameConstants {
    const val HEX_SIZE = 16f
    const val HEX_PICK_RADIUS_SQ = HEX_SIZE * HEX_SIZE

    const val MAP_WIDTH = 18
    const val MAP_HEIGHT = 15
    const val CITY_COUNT = 20
    const val CITY_PRODUCTION = 10
    const val CAPITAL_PRODUCTION = 20
    const val CAPITAL_PADDING_FACTOR = 0.12f

    const val MAX_MOVES_PER_TURN = 5
    const val MAX_TROOP_STRENGTH = 99

    const val INACTIVITY_TIMEOUT_SECONDS = 30f
    const val INACTIVITY_STRIKE_LIMIT = 3

    const val LOBBY_CODE_LENGTH = 6

    const val NETWORK_TIMEOUT_MS = 10_000L
}
