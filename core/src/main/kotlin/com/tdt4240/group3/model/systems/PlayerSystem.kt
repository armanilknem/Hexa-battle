package com.tdt4240.group3.model.systems

import com.tdt4240.group3.model.components.PlayerComponent
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import ktx.ashley.allOf
import ktx.ashley.get

class PlayerSystem : IteratingSystem(allOf(PlayerComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // This runs automatically for every player entity
        val player = entity[PlayerComponent.mapper]
        player?.let { p ->
            // Logic here

            // code below is temporary to test Ashley enginge!!
            Gdx.app.log("ECS", "Processing Player: ${p.name}")
        }
    }
}
