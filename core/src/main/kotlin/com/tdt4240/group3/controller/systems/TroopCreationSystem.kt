package com.tdt4240.group3.controller.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.NeedsTroopSpawnComponent
import com.tdt4240.group3.model.components.marker.SelectableComponent
import com.tdt4240.group3.model.entities.EntityFactory
import ktx.ashley.allOf
import ktx.ashley.get

class TroopCreationSystem(private val engine: Engine) : EntitySystem() {

    private val factory = EntityFactory(engine)
    private val cityFamily = allOf(CityComponent::class, PositionComponent::class, TeamComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class, NeedsTroopSpawnComponent::class).get()
    private val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()

    override fun update(deltaTime: Float) {
        val gameStateEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return

        createTroopsForTeam(gs.currentTeam)
        markSelectable(gs)
        gameStateEntity.remove(NeedsTroopSpawnComponent::class.java)
    }

    fun createTroopFromCity(cityEntity: Entity): Entity {
        return factory.createTroopFromCity(cityEntity)
    }

    fun createTroopsForTeam(team: TeamComponent.TeamName) {
    engine.getEntitiesFor(cityFamily)
        .filter { TeamComponent.mapper.get(it)?.team == team }
        .forEach {
            createTroopFromCity(it)
        }
    }

    fun markSelectable(gs: GameStateComponent) {
        engine.getEntitiesFor(troopFamily).forEach { troop ->
            troop.remove(SelectableComponent::class.java)
        }

        engine.getEntitiesFor(troopFamily)
            .filter { TeamComponent.mapper.get(it)?.team == gs.currentTeam }
            .forEach { troop ->
                troop.add(engine.createComponent(SelectableComponent::class.java))
            }
    }
}
