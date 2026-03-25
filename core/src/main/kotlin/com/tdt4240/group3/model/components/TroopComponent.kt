package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class TroopComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isMoved: Boolean = false
    var isClicked: Boolean = false

    fun isAttacked(attackPower: Int){
        this.strength -= attackPower
    }

    fun hasBeenMoved(){
        this.isMoved = true
    }

    fun hasBeenClicked(){
        this.isClicked = true
    }


    override fun reset() {
        isMoved = false
        isClicked = false
    }

    companion object {
        val mapper = mapperFor<TroopComponent>()
    }
}
