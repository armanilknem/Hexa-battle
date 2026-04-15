package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class GameStateComponent: Component, Pool.Poolable {
    var currentTeam: TeamComponent.TeamName = TeamComponent.TeamName.BLUE

    var turnCount: Int = 1

    override fun reset() {
        currentTeam = TeamComponent.TeamName.BLUE
        turnCount = 1
    }

    companion object {
        val mapper = mapperFor<GameStateComponent>()
    }
}
