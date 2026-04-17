package com.tdt4240.group3.config.team

object TeamCatalog {
    val teams = mapOf(
        "red" to TeamDefinition(
            key = "red",
            displayName = "Red",
            territoryColorHex = "#FF0000",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "red_troop.png",
                UnitTier.TIER_2 to "RedTank.png"
            )
        ),
        "blue" to TeamDefinition(
            key = "blue",
            displayName = "Blue",
            territoryColorHex = "#0000FF",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "blue_troop.png",
                UnitTier.TIER_2 to "BlueTank.png"
            )
        ),
        "green" to TeamDefinition(
            key = "green",
            displayName = "Green",
            territoryColorHex = "#00FF00",
            troopTextures = mapOf(
                UnitTier.TIER_1 to "troop.png",
                UnitTier.TIER_2 to "troop.png",
            )
        )
    )
}
