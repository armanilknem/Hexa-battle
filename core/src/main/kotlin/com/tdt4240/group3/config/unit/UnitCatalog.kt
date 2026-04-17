package com.tdt4240.group3.config.unit

object UnitCatalog {
    val units = mapOf(
        "baseTroop" to UnitDefinition(
            key = "baseTroop",
            displayName = "Base Troop",
            movement = MovementRules(
                moveRange = 2
            ),
            combat = CombatRules(
                maxStackSize = 99,
                canMergeFriendly = true
            )
        ),
        "tank" to UnitDefinition(
            key = "tank",
            displayName = "Tank",
            movement = MovementRules(
                moveRange = 3
            ),
            combat = CombatRules(
                attackMultiplier = 1.3f,
                defenseMultiplier = 1.2f
            )
        )
    )
}
