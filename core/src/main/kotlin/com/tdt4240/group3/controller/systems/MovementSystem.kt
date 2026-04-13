package com.tdt4240.group3.controller.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class MovementSystem(private val turnSystem: TurnSystem) : EntitySystem() {

    var onTurnEnd: (() -> Unit)? = null  // PlayScreen sets this
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()

    fun moveTroop(troop: Entity, targetTile: Entity) {
        val targetPos = targetTile[PositionComponent.mapper] ?: return
        val troopPos  = troop[PositionComponent.mapper] ?: return

        troopPos.q = targetPos.q
        troopPos.r = targetPos.r

        troop[TroopComponent.mapper]?.hasBeenMoved()

        if (allTroopsMoved()) {
            onTurnEnd?.invoke()
        }
    }
    private fun allTroopsMoved(): Boolean {
        return engine.getEntitiesFor(troopFamily)
            .filter { TeamComponent.mapper.get(it)?.team == turnSystem.currentTeam }
            .all { TroopComponent.mapper.get(it)?.isMoved == true }
    }

}
