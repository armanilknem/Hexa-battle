package com.tdt4240.group3.controller.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.MoveIntentComponent
import com.tdt4240.group3.model.components.marker.SelectableComponent
import ktx.ashley.allOf
import ktx.ashley.get

class MovementSystem : IteratingSystem(
    allOf(PositionComponent::class,
        TeamComponent::class,
        TroopComponent::class,
        MoveIntentComponent::class
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val pos = entity[PositionComponent.mapper] ?: return
        val intent = entity[MoveIntentComponent.mapper] ?: return

        pos.q = intent.targetQ
        pos.r = intent.targetR

        entity.remove(SelectableComponent::class.java)
        entity.remove(MoveIntentComponent::class.java)
    }
}
