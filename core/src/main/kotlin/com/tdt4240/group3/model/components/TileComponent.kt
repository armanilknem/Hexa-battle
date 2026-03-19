package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
class TileComponent : Component{
    enum class TileType { GRASS, WATER }

    var type: TileType = TileType.GRASS
}
