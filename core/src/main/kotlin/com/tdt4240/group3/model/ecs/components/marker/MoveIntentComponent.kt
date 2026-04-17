package com.tdt4240.group3.model.ecs.components.marker

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class MoveIntentComponent: Component, Pool.Poolable {
    var targetQ: Int = 0
    var targetR: Int = 0

    override fun reset() {
        targetQ = 0
        targetR = 0
    }

    companion object {
        val mapper = mapperFor<MoveIntentComponent>()
    }
}
