package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class MovementComponent : Component, Pool.Poolable {
    var moveRange: Int = 0
    var canCrossWater: Boolean = false

    override fun reset() {
        moveRange = 0
        canCrossWater = false
    }
}
