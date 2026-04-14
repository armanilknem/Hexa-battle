package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class TroopComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isColliding: Boolean = false

    fun colliding(){
        this.isColliding = true
    }

    override fun reset() {
        isColliding = false
    }

    fun resetForNewTurn(){
        isColliding = false
    }

    companion object {
        val mapper = mapperFor<TroopComponent>()
    }
}
