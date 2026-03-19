package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.Team ///NOO
import ktx.ashley.entity
import ktx.ashley.with

class EntityFactory(private val engine: Engine) {

    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent> {
            this.name = name
        }
    }

    fun createCity(name: String, team: Team, isCapital: Boolean, baseProduction: Int, x: Int, y: Int) = engine.entity {
        with<CityComponent> {
            this.name = name
            this.team = team
            this.baseProduction = baseProduction
            this.isCapital = isCapital
        }
        with<PositionComponent> {
            this.x = x
            this.y = y
        }
    }
}
