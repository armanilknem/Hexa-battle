package com.tdt4240.group3.config.unit

import com.tdt4240.group3.model.UnitType


object UnitCatalog {
    val units = mapOf(
        UnitType.SOLDIER to UnitDefinition(
            displayName = "Base Troop",
            movement = MovementRules(moveRange = 2),
            combat = CombatRules(
                attackMultiplier = 1.0f,
                defenseMultiplier = 1.0f
            )
        ),
        UnitType.TANK to UnitDefinition(
            displayName = "Tank",
            movement = MovementRules(moveRange = 2),
            combat = CombatRules(
                attackMultiplier = 1.3f,
                defenseMultiplier = 1.2f
            )
        ),
        UnitType.PLANE to UnitDefinition(
            displayName = "Plane",
            movement = MovementRules(moveRange = 2),
            combat = CombatRules(
                attackMultiplier = 1.6f,
                defenseMultiplier = 0.9f
            )
        )
    )
}
