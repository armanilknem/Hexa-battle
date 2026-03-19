package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

enum class Team {RED, GREEN, BLUE, PURPLE}

class TroopsComponent : Component, Pool.Poolable {
    var positionX: Int = 0
    var positionY: Int = 0
    var positionZ: Int = 0
    var strength: Int = 0
    var isMoved: Boolean = false
    var isClicked: Boolean = false
    var team: Team = Team.RED

    override fun reset() {
        positionX = 0
        positionY = 0
        positionZ = 0
        strength = 0
        isMoved = false
        isClicked = false
        team = Team.RED
    }

    companion object {
        val mapper = mapperFor<TroopsComponent>()
    }
}
