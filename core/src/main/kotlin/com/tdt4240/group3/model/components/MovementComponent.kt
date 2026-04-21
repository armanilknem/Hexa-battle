package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Movement stats for a troop entity. Initialised from [com.tdt4240.group3.config.unit.MovementRules]
 *  at spawn time; may be updated by [com.tdt4240.group3.model.systems.UnitPromotionSystem]. */
class MovementComponent : Component {
    var moveRange: Int = 0
    var canCrossWater: Boolean = false

    companion object { val mapper = mapperFor<MovementComponent>() }
}
