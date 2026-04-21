package com.tdt4240.group3.config.unit

/** Movement capabilities for a unit type. [moveRange] is the maximum hex distance per turn. */
data class MovementRules(
    val moveRange: Int = 2,
    val canCrossWater: Boolean = false
)

/**
 * Combat statistics for a unit type.
 * Attack and defense power = troop strength × respective multiplier.
 * [maxStackSize] caps the total strength when two friendly units merge.
 */
data class CombatRules(
    val maxStackSize: Int = 99,
    val attackMultiplier: Float = 1f,
    val defenseMultiplier: Float = 1f,
    val canMergeFriendly: Boolean = true
)

/** Full static definition of a unit type, combining display name, movement, and combat rules. */
data class UnitDefinition(
    val displayName: String,
    val movement: MovementRules,
    val combat: CombatRules
)
