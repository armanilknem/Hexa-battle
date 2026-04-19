package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.Team
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * System that checks if a win condition has been met.
 * A team wins when they control all capital cities on the map.
 */
class WinSystem : EntitySystem() {
    var onWin: ((Team) -> Unit)? = null
    private var winTriggered = false

    private val capitalFamily = allOf(CapitalComponent::class, TeamComponent::class).get()

    override fun update(deltaTime: Float) {
        if (winTriggered) return

        val capitals = engine.getEntitiesFor(capitalFamily)
        if (capitals.size() < 2) return

        // Check the owner of the first capital
        val firstOwner = capitals.first()[TeamComponent.mapper]?.team ?: return
        if (firstOwner == Team.NONE) return

        // Check if all other capitals are owned by the same team
        val allOwnedBySameTeam = capitals.all { it[TeamComponent.mapper]?.team == firstOwner }

        if (allOwnedBySameTeam) {
            winTriggered = true
            onWin?.invoke(firstOwner)
        }
    }

    /**
     * Resets the win state so the system can trigger again in a new game.
     */
    fun reset() {
        winTriggered = false
    }
}
