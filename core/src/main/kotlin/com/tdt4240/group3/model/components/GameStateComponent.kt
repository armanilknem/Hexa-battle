package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

class GameStateComponent : Component, Pool.Poolable {
    val activeTeams = mutableListOf<Team>()
    val eliminatedTeams = mutableSetOf<Team>()
    var turnCount: Int = 1
    var movesLeft: Int = 0

    var playerOrder: List<String> = emptyList()
    var currentPlayerIndex: Int = 0

    val currentTeam: Team
        get() = activeTeams.getOrElse(currentPlayerIndex) { Team.NONE }

    fun initialize(teams: List<Team>) {
        activeTeams.clear()
        activeTeams.addAll(teams.filter { it != Team.NONE })
        eliminatedTeams.clear()
        turnCount = 1
        movesLeft = 0

        playerOrder = emptyList()
        currentPlayerIndex = 0
    }

    override fun reset() {
        activeTeams.clear()
        eliminatedTeams.clear()
        turnCount = 1
        movesLeft = 0

        playerOrder = emptyList()
        currentPlayerIndex = 0
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
