package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.tdt4240.group3.model.UnitType
import ktx.ashley.mapperFor

/** Tracks the [UnitType] tier of a troop (SOLDIER → TANK → PLANE).
 *  Updated by [com.tdt4240.group3.model.systems.UnitPromotionSystem] when strength thresholds are crossed. */
class UnitComponent : Component {
    var unitType: UnitType = UnitType.SOLDIER

    companion object { val mapper = mapperFor<UnitComponent>() }
}
