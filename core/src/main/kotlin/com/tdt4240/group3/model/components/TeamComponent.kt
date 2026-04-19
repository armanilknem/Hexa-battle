package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

class TeamComponent : Component, Pool.Poolable {
    var team = Team.NONE

    override fun reset() {
        this.team = Team.NONE
    }

    companion object {
        val mapper = mapperFor<TeamComponent>()
    }
}
