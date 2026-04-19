package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.entity
import ktx.ashley.with

class CapitalFactory(private val engine: Engine): Factory<CapitalConfig> {
    override fun createEntity(config: CapitalConfig) = engine.entity {
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
        with<CapitalComponent> { } //TODO("Add an origin in capital component")
    }
}
