package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class PositionComponent : Component, Pool.Poolable {
    var x: Int = 0
    var y: Int = 0

    override fun reset() {
        x = 0
        y = 0
    }

    companion object {
        val mapper = mapperFor<PositionComponent>()
    }
}
