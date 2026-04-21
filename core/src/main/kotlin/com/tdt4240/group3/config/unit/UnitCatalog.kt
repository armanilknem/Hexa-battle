package com.tdt4240.group3.config.unit

import com.tdt4240.group3.model.UnitType

object UnitCatalog {
    val units = mapOf(
        UnitType.SOLDIER to UnitDefinition(
            displayName = "Soldier",
            movement = MovementRules(),
            combat   = CombatRules()
        ),
        UnitType.TANK to UnitDefinition(
            displayName = "Tank",
            movement = MovementRules(),
            combat = CombatRules(
                attackMultiplier  = 1.5f,
                defenseMultiplier = 1.5f
            )
        ),
        UnitType.PLANE to UnitDefinition(
            displayName = "Plane",
            movement = MovementRules(),
            combat = CombatRules(
                attackMultiplier  = 1.8f,
                defenseMultiplier = 1.8f
            )
        )
    )
}
