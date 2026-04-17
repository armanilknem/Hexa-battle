package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.Team
import ktx.ashley.allOf
import ktx.ashley.get

class WinSystem : EntitySystem() {
    var onWin: ((Team) -> Unit)? = null
    private var winTriggered = false

    override fun update(deltaTime: Float) {
        if (winTriggered) return

        // Find all capital entities
        val capitals = engine.getEntitiesFor(allOf(CapitalComponent::class).get()).toList()
        if (capitals.isEmpty()) return

        // Check if all capitals are owned by the same (non-NONE) team
        val firstOwner = capitals.first()[TeamComponent.mapper]?.team ?: return
        if (firstOwner == Team.NONE) return

        val allOwnedBySameTeam = capitals.all { it[TeamComponent.mapper]?.team == firstOwner }

        if (allOwnedBySameTeam) {
            winTriggered = true
            onWin?.invoke(firstOwner)
        }
    }
}
