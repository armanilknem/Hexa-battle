package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.tdt4240.group3.model.Team
import ktx.ashley.mapperFor

/** Marks a city as a capital. [originalTeam] records the founding team so the
 *  conqueror can be identified when this capital is captured. */
class CapitalComponent : Component {
    var originalTeam: Team = Team.NONE

    companion object { val mapper = mapperFor<CapitalComponent>() }
}
