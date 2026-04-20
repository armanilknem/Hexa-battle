package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.config.GameConstants
import ktx.ashley.mapperFor
import kotlin.math.sqrt

class PositionComponent : Component, Pool.Poolable {

    var q: Int = 0
    var r: Int = 0
    var prevQ: Int = -1
    var prevR: Int = -1
    var zIndex: Int = 0

    val x: Float get() = GameConstants.HEX_SIZE * (sqrt(3.0).toFloat() * q + sqrt(3.0).toFloat() / 2f * r)
    val y: Float get() = GameConstants.HEX_SIZE * (3f / 2f * r)
    val s: Int get() = -q - r

    override fun reset() {
        q = 0
        r = 0
        prevQ = -1
        prevR = -1
        zIndex = 0
    }

    companion object {
        val mapper = mapperFor<PositionComponent>()
    }
}
