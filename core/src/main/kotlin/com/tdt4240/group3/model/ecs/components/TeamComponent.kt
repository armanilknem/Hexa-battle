package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.mapperFor

class TeamComponent : Component, Pool.Poolable {
    var team = TeamName.NONE

    override fun reset() {
        this.team = TeamName.NONE
    }

    companion object {
        val mapper = mapperFor<TeamComponent>()
    }
}
