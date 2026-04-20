package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.config.ZIndex
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.entity
import ktx.ashley.with

class CityFactory(private val engine: Engine) : EntityFactory<CityConfig> {
    override fun createEntity(config: CityConfig) = engine.entity {
        with<CityComponent> {
            this.name = config.name
            this.baseProduction = config.baseProduction
        }
        with<PositionComponent> {
            this.q = config.q
            this.r = config.r
            this.zIndex = ZIndex.CITY
        }
        with<TeamComponent> {
            this.team = config.team
        }
    }
}
