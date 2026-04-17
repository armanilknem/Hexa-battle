package com.tdt4240.group3.model.unit

data class UnitDefinition(
    val key: String,
    val displayName: String,
    val movement: MovementRules,
    val combat: CombatRules
)
