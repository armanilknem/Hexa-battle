package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.TeamComponent

class TurnSystem : EntitySystem() {

    var currentTeam: TeamComponent.TeamName = TeamComponent.TeamName.BLUE
        private set

    fun endTurn() {
        currentTeam = when (currentTeam) {
            TeamComponent.TeamName.BLUE -> TeamComponent.TeamName.RED
            TeamComponent.TeamName.RED  -> TeamComponent.TeamName.BLUE
            else -> TeamComponent.TeamName.BLUE
        }
    }

    fun isCurrentTeam(team: TeamComponent.TeamName) = team == currentTeam
}
