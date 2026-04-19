package com.tdt4240.group3.model.temporaryFactory

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.ecs.components.CityComponent
import com.tdt4240.group3.model.ecs.components.PositionComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import ktx.ashley.entity
import ktx.ashley.with

class CityFactory(private val engine: Engine): Factory<CityConfig> {
    override fun createEntity(config: CityConfig) = engine.entity {
        with<CityComponent> {
            this.name = config.name
            this.baseProduction = config.baseProduction
        }
        with<PositionComponent> {
            this.q = config.q
            this.r = config.r
            this.zIndex = 1 // Middle layer //TODO("should be changed to some sort of global variable for better clarity")
        }
        with<TeamComponent> {
            this.team = config.team
        }
    }
}
