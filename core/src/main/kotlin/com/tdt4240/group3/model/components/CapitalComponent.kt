package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

class CapitalComponent : Component, Pool.Poolable {
    var originalTeam: Team = Team.NONE

    override fun reset() {
        originalTeam = Team.NONE
    }

    companion object {
        val mapper = mapperFor<CapitalComponent>()
    }
}
