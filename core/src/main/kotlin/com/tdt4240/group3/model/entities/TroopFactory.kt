package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.config.ZIndex
import com.tdt4240.group3.config.unit.UnitCatalog
import com.tdt4240.group3.model.components.CombatComponent
import com.tdt4240.group3.model.components.MovementComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.UnitType
import com.tdt4240.group3.model.components.UnitComponent
import ktx.ashley.entity
import ktx.ashley.with

class TroopFactory(private val engine: Engine) : EntityFactory<TroopConfig> {
    override fun createEntity(config: TroopConfig) = engine.entity {
        val unitDef = UnitCatalog.units.getValue(UnitType.SOLDIER)
        with<UnitComponent> {
            this.unitType = UnitType.SOLDIER
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
            this.zIndex = ZIndex.TROOP
        }
        with<TeamComponent> {
            this.team = config.team
        }
    }
}
