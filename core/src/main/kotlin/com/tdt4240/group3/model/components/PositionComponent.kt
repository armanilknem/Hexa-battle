package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class PositionComponent : Component, Pool.Poolable {
    var q: Int = 0
    var r: Int = 0
    var zIndex: Int = 0

    override fun reset() {
        q = 0
        r = 0
        zIndex = 0
    }

    companion object {
        val mapper = mapperFor<PositionComponent>()
    }
}
