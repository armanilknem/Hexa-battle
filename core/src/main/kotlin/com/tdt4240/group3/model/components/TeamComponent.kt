package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class TeamComponent : Component, Pool.Poolable{
    enum class TeamName {
        RED,
        BLUE,
        NONE
    }
    var team = TeamName.NONE

    override fun reset() {
        this.team = TeamName.NONE
    }
}
