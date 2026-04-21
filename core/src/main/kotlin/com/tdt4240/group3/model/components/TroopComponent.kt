package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Marks an entity as a troop and holds its current strength (stack size). */
class TroopComponent : Component {
    var strength: Int = 0

    companion object { val mapper = mapperFor<TroopComponent>() }
}
