package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class CombatComponent : Component, Pool.Poolable {
    var maxStackSize: Int = 99
    var attackMultiplier: Float = 1f
    var defenseMultiplier: Float = 1f
    var canMergeFriendly: Boolean = true

    override fun reset() {
        maxStackSize = 99
        attackMultiplier = 1f
        defenseMultiplier = 1f
        canMergeFriendly = true
    }

    companion object {
        val mapper = mapperFor<CombatComponent>()
    }
}
