package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

class GameStateComponent: Component, Pool.Poolable {
    val activeTeams = mutableListOf<Team>()
    var currentTeamIndex: Int = 0
    var turnCount: Int = 1
    var movesLeft: Int = 5

    val currentTeam: Team
        get() = activeTeams.getOrElse(currentTeamIndex) { Team.NONE }

    fun initialize(teams: List<Team>) {
        activeTeams.clear()
        activeTeams.addAll(teams.filter { it != Team.NONE })
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
