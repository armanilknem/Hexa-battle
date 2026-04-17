package com.tdt4240.group3.model.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class TouchInputComponent : Component, Pool.Poolable {
    var x: Float = 0f
    var y: Float = 0f
    // var inputType: InputType = InputType.TAP

    override fun reset() {
        x = 0f
        y = 0f
    }
    companion object {
        val mapper = mapperFor<TouchInputComponent>()
    }
}
