package com.tdt4240.group3.model.systems

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
    private val troopFamily = allOf(TroopComponent::class, PositionComponent::class).get()


    fun createTroopFromCity(cityEntity: Entity) {
        val cityComp = cityEntity[CityComponent.mapper] ?: return
        val cityPos = cityEntity[PositionComponent.mapper] ?: return
        val cityTeam = cityEntity[TeamComponent.mapper]?.team ?: return
        val exisingTroop = engine.getEntitiesFor(troopFamily).find {
            val p = it[PositionComponent.mapper]
            p?.q == cityPos.q && p?.r == cityPos.r
        }
        if (exisingTroop != null) {
            val troopComp = exisingTroop[TroopComponent.mapper]!!
            val troopTeam = exisingTroop[TeamComponent.mapper]?.team ?: return
            if (troopTeam == cityTeam) {
                val total = troopComp.strength + cityComp.baseProduction
                if (total <= 99) {
                    troopComp.strength = total
                }
                else {
                    troopComp.strength = 99
                }
            }
        }
        else {
            factory.createTroopFromCity(cityEntity)
        }
    }

    fun createTroopsForTeam(team: TeamComponent.TeamName) {
    engine.getEntitiesFor(cityFamily)
        .filter { TeamComponent.mapper.get(it)?.team == team }
        .forEach { createTroopFromCity(it) }
    }
}
