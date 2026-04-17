package com.tdt4240.group3.model.components.marker

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class TerritoryComponent: Component, Pool.Poolable {
    override fun reset() = Unit

    companion object {
        val mapper = mapperFor<TerritoryComponent>()
    }
}
