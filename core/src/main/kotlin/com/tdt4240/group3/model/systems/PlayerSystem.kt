package com.tdt4240.group3.model.systems

import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.components.PlayerComponent
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerSystem : IteratingSystem(allOf(PlayerComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
    }
}
