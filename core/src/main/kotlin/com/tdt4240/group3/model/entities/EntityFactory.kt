package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.tdt4240.group3.model.components.Team
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.TroopsComponent
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.entity
import ktx.ashley.with

class EntityFactory(private val engine: Engine) {

    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent > {
            this.name = name
        }
    }

    fun createCity(name: String, isCapital: Boolean, baseProduction: Int, x: Int, y: Int, team: TeamComponent.TeamName) = engine.entity {
        with<CityComponent> {
            this.name = name
            this.baseProduction = baseProduction
            this.isCapital = isCapital
        }
        with<PositionComponent> {
            this.x = x
            this.y = y
        }
        with<TeamComponent> {
            this.team = team
        }
    }

    fun createTroop(positionX: Int, positionY: Int, positionZ: Int, team: Team, strength: Int) = engine.entity {
        with<TroopsComponent> {
            this.positionX = positionX
            this.positionY = positionY
            this.positionZ = positionZ
            this.team = team
            this.strength = strength
        }
    }
}
