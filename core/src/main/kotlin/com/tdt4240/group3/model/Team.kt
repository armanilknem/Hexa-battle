package com.tdt4240.group3.model

/**
 * Represents the different teams in the game.
 * [NONE] is used as a neutral/unowned sentinel — it is not a playable faction.
 *
 * Visual properties such as territory colours and unit textures for each team are
 * defined in [com.tdt4240.group3.view.styleRegistries.TeamVisualRegistry].
 */
enum class Team {
    RED,
    BLUE,
    GREEN,
    PURPLE,
    NONE
}
