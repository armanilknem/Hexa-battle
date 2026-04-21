package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.config.ZIndex
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.entity
import ktx.ashley.with

/** Creates a city entity that additionally carries a [CapitalComponent] marker. */
class CapitalFactory(private val engine: Engine) : EntityFactory<CityConfig> {
    override fun createEntity(config: CityConfig) = engine.entity {
        with<CityComponent> {
            name = config.name
            baseProduction = config.baseProduction
        }
        with<PositionComponent> {
            q = config.q
            r = config.r
            zIndex = ZIndex.CITY
        }
        with<TeamComponent> {
            team = config.team
        }
        with<CapitalComponent> {}
    }
}
