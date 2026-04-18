package com.tdt4240.group3.model.ecs.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.ecs.components.CombatComponent
import com.tdt4240.group3.model.ecs.components.MovementComponent
import com.tdt4240.group3.model.ecs.components.PositionComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.components.TroopComponent
import com.tdt4240.group3.model.ecs.components.UnitComponent
import com.tdt4240.group3.model.unit.UnitCatalog
import ktx.ashley.entity
import ktx.ashley.with

class TroopFactory(private val engine: Engine) : Factory<TroopConfig> {
    override fun createEntity(config: TroopConfig) = engine.entity {
        val unitDef = UnitCatalog.units.getValue(config.unitKey)
        with<UnitComponent> {
            this.unitKey = unitDef.key
        }
        with<MovementComponent> {
            this.moveRange = unitDef.movement.moveRange
            this.canCrossWater = unitDef.movement.canCrossWater
        }
        with<CombatComponent> {
            this.maxStackSize = unitDef.combat.maxStackSize
            this.attackMultiplier = unitDef.combat.attackMultiplier
            this.defenseMultiplier = unitDef.combat.defenseMultiplier
            this.canMergeFriendly = unitDef.combat.canMergeFriendly
        }
        with<TroopComponent> {
            this.strength = config.strength
        }
        with<PositionComponent> {
            this.q = config.q
            this.r = config.r
            this.zIndex = 2 // Top layer //TODO should be changed to some sort of global variable for better clarity
        }
        with<TeamComponent> {
            this.team = config.team
        }
    }
}
