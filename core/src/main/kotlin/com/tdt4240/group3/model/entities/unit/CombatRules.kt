package com.tdt4240.group3.model.entities.unit

data class CombatRules(
    val maxStackSize: Int = 99,
    val attackMultiplier: Float = 1f,
    val defenseMultiplier: Float = 1f,
    val canMergeFriendly: Boolean = true
)
