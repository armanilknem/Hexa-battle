package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class PlayerComponent : Component, Pool.Poolable {
    var name: String = ""

    var hasTurn: Boolean = false

    override fun reset() {
        name = ""
    }

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
