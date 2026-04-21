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
            unitType = UnitType.SOLDIER
        }
        with<MovementComponent> {
            moveRange = unitDef.movement.moveRange
            canCrossWater = unitDef.movement.canCrossWater
        }
        with<CombatComponent> {
            maxStackSize = unitDef.combat.maxStackSize
            attackMultiplier = unitDef.combat.attackMultiplier
            defenseMultiplier = unitDef.combat.defenseMultiplier
            canMergeFriendly = unitDef.combat.canMergeFriendly
        }
        with<TroopComponent> {
            strength = config.strength
        }
        with<PositionComponent> {
            q = config.q
            r = config.r
            zIndex = ZIndex.TROOP
        }
        with<TeamComponent> {
            team = config.team
        }
    }
}
