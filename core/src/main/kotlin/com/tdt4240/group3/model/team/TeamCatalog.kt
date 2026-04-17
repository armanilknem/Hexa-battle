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
                UnitTier.TIER_1 to "troopSprites/red_soldier.png",
                UnitTier.TIER_2 to "troopSprites/red_tank.png",
                UnitTier.TIER_3 to "troopSprites/red_plane.png"
            )
        ),
        "blue" to TeamDefinition(
            teamName = TeamName.BLUE,
            displayName = "Blue",
            territoryColorHex = "#0000FF",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "troopSprites/blue_soldier.png",
                UnitTier.TIER_2 to "troopSprites/blue_tank.png",
                UnitTier.TIER_3 to "troopSprites/blue_plane.png"
            )
        ),
        "green" to TeamDefinition(
            teamName = TeamName.GREEN,
            displayName = "Green",
            territoryColorHex = "#00FF00",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "troopSprites/green_soldier.png",
                UnitTier.TIER_2 to "troopSprites/green_tank.png",
                UnitTier.TIER_3 to "troopSprites/green_plane.png"
            )
        )
    )
}
