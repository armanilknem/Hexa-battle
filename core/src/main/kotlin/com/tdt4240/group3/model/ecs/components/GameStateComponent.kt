package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.mapperFor

class GameStateComponent: Component, Pool.Poolable {
    val activeTeams = mutableListOf<TeamName>()
    var currentTeamIndex: Int = 0
    var turnCount: Int = 1
    var movesLeft: Int = 5

    val currentTeam: TeamName
        get() = activeTeams.getOrElse(currentTeamIndex) { TeamName.NONE }

    fun initialize(teams: List<TeamName>) {
        activeTeams.clear()
        activeTeams.addAll(teams.filter { it != TeamName.NONE })
        currentTeamIndex = 0
        turnCount = 1
    }

    override fun reset() {
        activeTeams.clear()
        currentTeamIndex = 0
        turnCount = 1
        movesLeft = 5
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
