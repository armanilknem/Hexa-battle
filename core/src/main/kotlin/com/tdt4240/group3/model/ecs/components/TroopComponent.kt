package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class TroopComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isHighlighted: Boolean = false

    override fun reset() {
    }

    companion object {
        val mapper = mapperFor<TroopComponent>()
    }
}
