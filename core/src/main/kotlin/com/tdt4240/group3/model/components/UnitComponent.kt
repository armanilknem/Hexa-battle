package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import com.tdt4240.group3.model.UnitType
import ktx.ashley.mapperFor

class UnitComponent : Component, Pool.Poolable {
    var unitType: UnitType = UnitType.SOLDIER

    override fun reset() {
        unitType = UnitType.SOLDIER
    }

    companion object {
        val mapper = mapperFor<UnitComponent>()
    }
}
