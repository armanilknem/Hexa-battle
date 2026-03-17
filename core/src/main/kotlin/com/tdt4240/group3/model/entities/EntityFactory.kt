package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.badlogic.ashley.core.Engine
import ktx.ashley.entity
import ktx.ashley.with



class EntityFactory(private val engine: Engine) {

    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent> {
            this.name = name
        }
    }


}
