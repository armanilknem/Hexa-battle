package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.tdt4240.group3.config.GameConstants
import ktx.ashley.mapperFor
import kotlin.math.sqrt

/**
 * Hex-grid coordinates for any entity placed on the map.
 *
 * [q] and [r] are axial (offset) coordinates; [s] is the derived cube coordinate.
 * [x] and [y] are the corresponding world-space pixel positions (pointy-top hex layout).
 * [prevQ]/[prevR] record the position before the last move; sentinel −1 means "not yet moved".
 * [zIndex] controls draw order within the same rendering pass.
 */
class PositionComponent : Component {

    var q: Int = 0
    var r: Int = 0
    var prevQ: Int = -1
    var prevR: Int = -1
    var zIndex: Int = 0

    val x: Float get() = GameConstants.HEX_SIZE * (SQRT3 * q + SQRT3 / 2f * r)
    val y: Float get() = GameConstants.HEX_SIZE * (3f / 2f * r)
    val s: Int  get() = -q - r

    companion object {
        val mapper = mapperFor<PositionComponent>()
        private val SQRT3 = sqrt(3.0).toFloat()
    }
}
