package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import kotlin.math.sqrt

class PositionComponent : Component, Pool.Poolable {

    val size = 16f
    var q: Int = 0
    var r: Int = 0
    var prevQ: Int = 0
    var prevR: Int = 0
    var zIndex: Int = 0

    val x: Float get() = size * (sqrt(3.0).toFloat() * q + sqrt(3.0).toFloat() / 2f * r)
    val y: Float get() = size * (3f / 2f * r)
    // Cube coordinate S is derived: s = -q - r
    val s: Int get() = -q - r

    override fun reset() {
        q = 0
        r = 0
        prevQ = 0
        prevR = 0
        zIndex = 0
    }

    companion object {
        val mapper = mapperFor<PositionComponent>()
    }
}
