package com.tdt4240.group3.config.unit

import com.tdt4240.group3.model.UnitType


object UnitCatalog {
    val units = mapOf(
        UnitType.SOLDIER to UnitDefinition(
            displayName = "Base Troop",
            movement = MovementRules(),
            combat = CombatRules()
        ),
        UnitType.TANK to UnitDefinition(
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
