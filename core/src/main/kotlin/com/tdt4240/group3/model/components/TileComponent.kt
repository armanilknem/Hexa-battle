package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor

class TileComponent : Component, Poolable {
    enum class TileType { GRASS, WATER }

    var type: TileType = TileType.GRASS

    override fun reset() {
        type = TileType.GRASS
    }

    companion object {
        val mapper = mapperFor<TileComponent>()
    }
}
