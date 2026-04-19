package com.tdt4240.group3.model.components.marker

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class SelectableComponent: Component, Pool.Poolable {
    override fun reset() {}

    companion object {
        val mapper = mapperFor<SelectableComponent>()
    }
}
