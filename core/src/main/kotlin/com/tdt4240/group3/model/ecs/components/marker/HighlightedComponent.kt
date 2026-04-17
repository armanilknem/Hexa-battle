package com.tdt4240.group3.model.ecs.components.marker

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class HighlightedComponent: Component, Pool.Poolable {
    override fun reset() = Unit

    companion object {
        val mapper = mapperFor<HighlightedComponent>()
    }
}
