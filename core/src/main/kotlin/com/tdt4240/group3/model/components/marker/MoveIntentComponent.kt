package com.tdt4240.group3.model.components.marker

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Attached to a troop entity while the player has issued a move order but it has not yet resolved. */
class MoveIntentComponent : Component {
    var targetQ: Int = 0
    var targetR: Int = 0

    companion object { val mapper = mapperFor<MoveIntentComponent>() }
}
