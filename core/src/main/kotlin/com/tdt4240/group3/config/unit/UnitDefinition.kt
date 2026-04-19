package com.tdt4240.group3.config.unit

data class UnitDefinition(
    val displayName: String,
    val movement: MovementRules,
    val combat: CombatRules
)
