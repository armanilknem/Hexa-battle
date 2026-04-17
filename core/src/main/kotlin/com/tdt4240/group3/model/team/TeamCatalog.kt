package com.tdt4240.group3.model.team

/**
 * Registry for static team definitions.
 * Centralizes configuration for team-specific visuals and display names.
 */
object TeamCatalog {
    val teams = mapOf(
        "red" to TeamDefinition(
            teamName = TeamName.RED,
            displayName = "Red",
            territoryColorHex = "#FF0000",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "red_troop.png",
                UnitTier.TIER_2 to "RedTank.png"
            )
        ),
        "blue" to TeamDefinition(
            teamName = TeamName.BLUE,
            displayName = "Blue",
            territoryColorHex = "#0000FF",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "blue_troop.png",
                UnitTier.TIER_2 to "BlueTank.png"
            )
        ),
        "green" to TeamDefinition(
            teamName = TeamName.GREEN,
            displayName = "Green",
            territoryColorHex = "#00FF00",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "troop.png",
                UnitTier.TIER_2 to "troop.png",
            )
        )
    )
}
