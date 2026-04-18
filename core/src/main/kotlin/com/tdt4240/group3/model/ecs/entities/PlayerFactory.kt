package com.tdt4240.group3.model.ecs.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.ecs.components.PlayerComponent
import ktx.ashley.entity
import ktx.ashley.with

//TODO see if players are ever created using the factory

class PlayerFactory(private val engine: Engine): Factory<PlayerConfig> {
    override fun createEntity(config: PlayerConfig) = engine.entity {
        with<PlayerComponent> {
            this.name = config.name
        }
    }
}
