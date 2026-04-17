package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

/**
 * name
 */

class UnitComponent : Component, Pool.Poolable {
    var unitKey: String = ""

    override fun reset() {
        unitKey = ""
    }

    companion object {
        val mapper = mapperFor<UnitComponent>()
    }
}
