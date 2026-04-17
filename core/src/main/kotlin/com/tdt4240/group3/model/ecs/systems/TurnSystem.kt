package com.tdt4240.group3.model.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get

class TurnSystem : EntitySystem() {
    private val gameStateFamily = allOf(GameStateComponent::class).get()

    override fun update(deltaTime: Float) {
        val gameStateEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return

        if (gameStateEntity.getComponent(NeedsTroopSpawnComponent::class.java) != null) {
            return
        }

        val selectableTroops = engine.getEntitiesFor(allOf(SelectableComponent::class).get()).toList()

        if (selectableTroops.isEmpty() || gs.movesLeft < 1) {
            endTurn()
        }
    }

    fun endTurn() {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameState[GameStateComponent.mapper] ?: return

        gs.currentTeam = when (gs.currentTeam) {
            TeamComponent.TeamName.BLUE -> TeamComponent.TeamName.RED
            TeamComponent.TeamName.RED -> TeamComponent.TeamName.BLUE
            else -> throw IllegalStateException("currentTeam should never be NONE")
        }

        if (gs.currentTeam == TeamComponent.TeamName.BLUE) {
            gs.turnCount++
        }
        gs.movesLeft = 5
        requestTroopSpawn(gameState)
    }

    fun isCurrentTeam(team: TeamComponent.TeamName): Boolean {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return false
        val gs = gameState[GameStateComponent.mapper] ?: return false
        return gs.currentTeam == team
    }

    private fun requestTroopSpawn(gameStateEntity: Entity) {
        if (gameStateEntity.getComponent(NeedsTroopSpawnComponent::class.java) == null) {
            gameStateEntity.add(engine.createComponent(NeedsTroopSpawnComponent::class.java))
        }
    }

}
