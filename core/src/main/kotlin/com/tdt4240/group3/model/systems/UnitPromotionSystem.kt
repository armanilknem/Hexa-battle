package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.config.unit.UnitCatalog
import com.tdt4240.group3.model.UnitType
import com.tdt4240.group3.model.components.CombatComponent
import com.tdt4240.group3.model.components.MovementComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.UnitComponent
import ktx.ashley.allOf
import ktx.ashley.get

class UnitPromotionSystem : EntitySystem() {

    private val troopFamily = allOf(
        TroopComponent::class,
        UnitComponent::class,
        MovementComponent::class,
        CombatComponent::class
    ).get()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(troopFamily).forEach { entity ->
            val troop    = entity[TroopComponent.mapper]    ?: return@forEach
            val unit     = entity[UnitComponent.mapper]     ?: return@forEach
            val movement = entity[MovementComponent.mapper] ?: return@forEach
            val combat   = entity[CombatComponent.mapper]   ?: return@forEach

            val targetType = unitTypeForStrength(troop.strength)
            if (targetType == unit.unitType) return@forEach

            val def = UnitCatalog.units.getValue(targetType)
            unit.unitType            = targetType
            movement.moveRange       = def.movement.moveRange
            movement.canCrossWater   = def.movement.canCrossWater
            combat.attackMultiplier  = def.combat.attackMultiplier
            combat.defenseMultiplier = def.combat.defenseMultiplier
            combat.maxStackSize      = def.combat.maxStackSize
            combat.canMergeFriendly  = def.combat.canMergeFriendly
        }
    }

    companion object {
        fun unitTypeForStrength(strength: Int): UnitType = when {
            strength >= GameConstants.PLANE_PROMOTION_THRESHOLD -> UnitType.PLANE
            strength >= GameConstants.TANK_PROMOTION_THRESHOLD  -> UnitType.TANK
            else                                                -> UnitType.SOLDIER
        }
    }
}
