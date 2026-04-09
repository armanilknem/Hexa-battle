package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.entities.EntityFactory

class TroopCreationSystem(private val engine: Engine) : EntitySystem() {

    private val factory = EntityFactory(engine)

    fun createTroopFromCity(cityEntity: Entity): Entity {
        return factory.createTroopFromCity(cityEntity)
    }
}
