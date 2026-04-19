package com.tdt4240.group3.model

/**
 * Represents the different teams in the game.
 *
 * Visual properties such as territory colors and unit textures associated
 * with each team are defined in [com.tdt4240.group3.view.styleRegistries.TeamVisualRegistry].
 *
 * @property isPlayer True if the team is a playable faction, false otherwise (e.g., NONE).
 */
enum class Team(val isPlayer: Boolean = true) {
    RED,
    BLUE,
    GREEN,
    PURPLE,
    NONE(false)
}
