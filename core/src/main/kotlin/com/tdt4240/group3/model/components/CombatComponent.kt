package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Combat stats for a troop entity. Initialised from [com.tdt4240.group3.config.unit.CombatRules]
 *  at spawn time; may be updated by [com.tdt4240.group3.model.systems.UnitPromotionSystem]. */
class CombatComponent : Component {
    var maxStackSize: Int = 99
    var attackMultiplier: Float = 1f
    var defenseMultiplier: Float = 1f
    var canMergeFriendly: Boolean = true

    companion object { val mapper = mapperFor<CombatComponent>() }
}
