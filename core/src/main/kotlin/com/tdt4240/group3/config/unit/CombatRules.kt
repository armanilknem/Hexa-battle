package com.tdt4240.group3.config.unit

import com.tdt4240.group3.config.GameConstants

data class CombatRules(
    val maxStackSize: Int = GameConstants.MAX_TROOP_STRENGTH,
    val attackMultiplier: Float = 1f,
    val defenseMultiplier: Float = 1f,
    val canMergeFriendly: Boolean = true
)
