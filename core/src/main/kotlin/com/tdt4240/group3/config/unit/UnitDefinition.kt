package com.tdt4240.group3.config.unit

data class MovementRules(
    val moveRange: Int = 2,
    val canCrossWater: Boolean = false
)

data class CombatRules(
    val maxStackSize: Int = 99,
    val attackMultiplier: Float = 1f,
    val defenseMultiplier: Float = 1f,
    val canMergeFriendly: Boolean = true
)

data class UnitDefinition(
    val displayName: String,
    val movement: MovementRules,
    val combat: CombatRules
)
