package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor
class HexComponent : Component{
    companion object {
        val mapper = mapperFor<HexComponent>()
    }
    var q: Int = 0
    var r: Int = 0
    // Cube coordinate S is derived: s = -q - r
    val s: Int get() = -q - r
}
