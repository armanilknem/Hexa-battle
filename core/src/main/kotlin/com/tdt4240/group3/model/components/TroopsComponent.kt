package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

enum class Team {RED, GREEN, BLUE, PURPLE}

class TroopsComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isMoved: Boolean = false
    var isClicked: Boolean = false
    var team: Team = Team.RED

    override fun reset() {
        strength = 0
        isMoved = false
        isClicked = false
        team = Team.RED
    }

    companion object {
        val mapper = mapperFor<TroopsComponent>()
    }
}
