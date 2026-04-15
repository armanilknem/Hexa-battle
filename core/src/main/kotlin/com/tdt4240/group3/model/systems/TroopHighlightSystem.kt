package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class TroopHighlightSystem(private val turnSystem: TurnSystem) : EntitySystem() {

    private val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(troopFamily).forEach { entity ->
            val troop = entity[TroopComponent.mapper] ?: return@forEach
            val team  = entity[TeamComponent.mapper]  ?: return@forEach
            troop.isHighlighted = !troop.isMoved && team.team == turnSystem.currentTeam
        }
    }
}
