package com.tdt4240.group3.controller.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.entities.EntityFactory
import ktx.ashley.allOf

class TroopCreationSystem(private val engine: Engine) : EntitySystem() {

    private val factory = EntityFactory(engine)

    private val cityFamily = allOf(CityComponent::class, PositionComponent::class, TeamComponent::class).get()


    fun createTroopFromCity(cityEntity: Entity): Entity {
        return factory.createTroopFromCity(cityEntity)
    }

    fun createTroopsForTeam(team: TeamComponent.TeamName) {
    engine.getEntitiesFor(cityFamily)
        .filter { TeamComponent.mapper.get(it)?.team == team }
        .forEach { createTroopFromCity(it) }
    }
}
