package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class TeamComponent : Component, Pool.Poolable {
    enum class TeamName {
        RED,
        BLUE,
        GREEN,
        NONE
    }

    var team = TeamName.NONE

    override fun reset() {
        this.team = TeamName.NONE
    }

    companion object {
        val mapper = mapperFor<TeamComponent>()
    }
}
