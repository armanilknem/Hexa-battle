package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Identifies a hex tile entity and its terrain type. */
class TileComponent : Component {
    enum class TileType { GRASS, WATER }

    var type: TileType = TileType.GRASS

    companion object { val mapper = mapperFor<TileComponent>() }
}
