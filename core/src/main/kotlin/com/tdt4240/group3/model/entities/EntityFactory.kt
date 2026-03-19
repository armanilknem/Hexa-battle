package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.tdt4240.group3.model.components.Team
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.TroopsComponent
import ktx.ashley.entity
import ktx.ashley.with



class EntityFactory(private val engine: Engine) {

    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent > {
            this.name = name
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
