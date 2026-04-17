package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class PlayerComponent : Component, Pool.Poolable {
    var name: String = ""

    override fun reset() {
        name = ""
    }

    companion object {
        val mapper = mapperFor<PlayerComponent>()
    }
}
