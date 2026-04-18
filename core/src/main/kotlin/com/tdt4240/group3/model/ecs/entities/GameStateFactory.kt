package com.tdt4240.group3.model.ecs.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.ecs.components.GameStateComponent
import ktx.ashley.entity
import ktx.ashley.with

class GameStateFactory(private val engine: Engine): Factory<GameStateConfig> {
    override fun createEntity(config: GameStateConfig) = engine.entity {
        with<GameStateComponent> {
            initialize(config.activeTeams)
        }
    }
}
