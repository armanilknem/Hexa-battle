package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class TroopComponent : Component, Pool.Poolable {
    var strength: Int = 0
    var isMoved: Boolean = false
    var isClicked: Boolean = false
    var isColliding: Boolean = false
    var isHighlighted: Boolean = false


    fun colliding(){
        this.isColliding = true
    }

    fun hasBeenMoved(){
        this.isMoved = true
    }

    fun hasBeenClicked(){
        this.isClicked = true
    }



    override fun reset() {
        strength = 0
        isMoved = false
        isClicked = false
        isColliding = false
    }

    fun resetForNewTurn(){
        isMoved = false
        isClicked = false
        isColliding = false
        isHighlighted = false
    }


    companion object {
        val mapper = mapperFor<TroopComponent>()
    }
}
