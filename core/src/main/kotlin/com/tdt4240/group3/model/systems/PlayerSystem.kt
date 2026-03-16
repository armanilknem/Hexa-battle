package com.tdt4240.group3.model.systems

import com.tdt4240.group3.model.entities.EntityManager
import com.tdt4240.group3.model.components.PlayerComponent

class PlayerSystem(private val entityManager: EntityManager) {

    fun addScore(entityId: Int, points: Int) {
        val player = entityManager.getComponent(entityId, PlayerComponent::class.java)

        if (player != null) {
            player.score += points
            println("SYSTEM: Player ${player.name} gained $points points. Total: ${player.score}")
        }
    }


    fun update() {
        entityManager.getAllEntitiesWithComponents().forEach { (id, components) ->
            val player = components.filterIsInstance<PlayerComponent>().firstOrNull()
            if (player != null) {
                // Perform any per-frame logic here
                // e.g., print(player.name + " is active")
            }
        }
    }
}
