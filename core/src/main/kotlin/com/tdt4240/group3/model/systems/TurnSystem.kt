package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.TeamComponent

class TurnSystem : EntitySystem() {

    var currentTeam: TeamComponent.TeamName = TeamComponent.TeamName.BLUE
        private set

    var turnCount: Int = 1
        private set
    fun endTurn() {
        currentTeam = when (currentTeam) {
            TeamComponent.TeamName.BLUE -> TeamComponent.TeamName.RED
            TeamComponent.TeamName.RED  -> TeamComponent.TeamName.BLUE
            else -> throw IllegalStateException("currentTeam should never be NONE")
        }
        if (currentTeam == TeamComponent.TeamName.BLUE){
            turnCount++
        }
    }

    fun isCurrentTeam(team: TeamComponent.TeamName) = team == currentTeam
}
