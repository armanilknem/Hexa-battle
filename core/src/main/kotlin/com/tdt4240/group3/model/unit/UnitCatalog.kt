package com.tdt4240.group3.model.unit

/**
 * Registry for static unit definitions.
 * Used in entityFactory to create troops.
 */
object UnitCatalog {
    val units = mapOf(
        "baseTroop" to UnitDefinition(
            key = "baseTroop",
            displayName = "Base Troop",
            movement = MovementRules(),
            combat = CombatRules()
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
