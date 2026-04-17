package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
class CityComponent : Component, Pool.Poolable {
    var name: String = ""
    var baseProduction = 10
    var isCapital: Boolean = false

    override fun reset() {
        name = ""
        baseProduction = 10
        isCapital = false
    }

    companion object {
        val mapper = mapperFor<CityComponent>()
    }
}

