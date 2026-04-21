package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

/** Identifies which [Team] owns a troop, tile, or city entity. */
class TeamComponent : Component {
    var team = Team.NONE

    companion object { val mapper = mapperFor<TeamComponent>() }
}
