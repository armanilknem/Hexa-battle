package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class GameStateComponent: Component, Pool.Poolable {
    var currentTeam: TeamComponent.TeamName = TeamComponent.TeamName.BLUE

    var turnCount: Int = 1
    var movesLeft: Int = 5


    override fun reset() {
        currentTeam = TeamComponent.TeamName.BLUE
        turnCount = 1
        movesLeft = 5
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
