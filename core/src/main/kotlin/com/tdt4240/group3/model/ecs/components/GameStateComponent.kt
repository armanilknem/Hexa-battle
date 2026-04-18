package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.mapperFor

class GameStateComponent : Component, Pool.Poolable {
    val activeTeams = mutableListOf<TeamName>()
    var turnCount: Int = 1
    var movesLeft: Int = 5

    var playerOrder: List<String> = emptyList()
    var currentPlayerIndex: Int = 0

    val currentTeam: TeamName
        get() = activeTeams.getOrElse(currentPlayerIndex) { TeamName.NONE }

    fun initialize(teams: List<TeamName>) {
        activeTeams.clear()
        activeTeams.addAll(teams.filter { it != TeamName.NONE })
        turnCount = 1
        movesLeft = 5

        playerOrder = emptyList()
        currentPlayerIndex = 0
    }

    override fun reset() {
        activeTeams.clear()
        turnCount = 1
        movesLeft = 5

        playerOrder = emptyList()
        currentPlayerIndex = 0
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
