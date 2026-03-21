package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class TroopsComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isMoved: Boolean = false
    var isClicked: Boolean = false

    fun isAttacked(attackPower: Int){
        this.strength -= attackPower
    }

    fun setIsMoved(isMoved: Boolean){
        this.isMoved = isMoved
    }

    fun setIsClicked(isClicked: Boolean){
        this.isClicked = isClicked
    }


    override fun reset() {
        strength = 0
        isMoved = false
        isClicked = false
    }

    companion object {
        val mapper = mapperFor<TroopsComponent>()
    }
}
