package com.tdt4240.group3.controller.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf

class TurnSystem : EntitySystem() {

    var currentTeam: TeamComponent.TeamName = TeamComponent.TeamName.BLUE
        private set

    var turnCount: Int = 1
        private set

    private val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()

    fun endTurn() {
        resetTroopMoves()
        currentTeam = when (currentTeam) {
            TeamComponent.TeamName.BLUE -> TeamComponent.TeamName.RED
            TeamComponent.TeamName.RED  -> TeamComponent.TeamName.BLUE
            else -> throw IllegalStateException("currentTeam should never be NONE")
        }
        if (currentTeam == TeamComponent.TeamName.BLUE){
            turnCount++
        }
    }

    private fun resetTroopMoves() {
        engine.getEntitiesFor(troopFamily)
            .filter { TeamComponent.mapper.get(it)?.team == currentTeam }
            .forEach { TroopComponent.mapper.get(it)?.isMoved = false }
    }

    fun isCurrentTeam(team: TeamComponent.TeamName) = team == currentTeam
}
