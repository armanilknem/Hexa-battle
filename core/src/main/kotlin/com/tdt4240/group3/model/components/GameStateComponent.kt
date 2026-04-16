package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class GameStateComponent: Component, Pool.Poolable {
    val activeTeams = mutableListOf<TeamComponent.TeamName>()
    var currentTeamIndex: Int = 0
    var turnCount: Int = 1
    val currentTeam: TeamComponent.TeamName
        get() = activeTeams.getOrElse(currentTeamIndex) { TeamComponent.TeamName.NONE }

    fun initialize(teams: List<TeamComponent.TeamName>) {
        activeTeams.clear()
        activeTeams.addAll(teams.filter { it != TeamComponent.TeamName.NONE })
        currentTeamIndex = 0
        turnCount = 1
    }

    override fun reset() {
        activeTeams.clear()
        currentTeamIndex = 0
        turnCount = 1
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
