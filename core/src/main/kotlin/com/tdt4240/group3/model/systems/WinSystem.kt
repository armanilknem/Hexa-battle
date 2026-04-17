package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.CityComponent
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.get

class WinSystem : IteratingSystem(allOf(TeamComponent::class, CityComponent::class).get()) {
    var onWin: ((TeamComponent.TeamName) -> Unit)? = null
    private var winTriggered = false

    override fun update(deltaTime: Float) {
        if (winTriggered) return
        val capitals = entities.filter { it[CityComponent.mapper]?.isCapital == true }
        if (capitals.isEmpty()) return
        val owner = capitals.first()[TeamComponent.mapper]?.team ?: return
        if (owner == TeamComponent.TeamName.NONE) return
        if (capitals.all { it[TeamComponent.mapper]?.team == owner }) {
            winTriggered = true
            onWin?.invoke(owner)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {}
}
