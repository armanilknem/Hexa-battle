package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class CollisionSystem : IteratingSystem(allOf(TroopComponent::class, PositionComponent::class, TeamComponent::class).get()){

    override fun processEntity(movingEntity: Entity, deltaTime: Float) {
        val troop = movingEntity[TroopComponent.mapper]!!

        // Only process if the troop has actually moved this turn
        if (!troop.isColliding) return

        val pos = movingEntity[PositionComponent.mapper]!!
        val team = movingEntity[TeamComponent.mapper]!!

        // Find what is on the target tile (excluding the moving entity itself)
        val otherEntities = engine.getEntitiesFor(allOf(PositionComponent::class).get())
            .filter { it != movingEntity }
            .filter {
                val otherPos = it[PositionComponent.mapper]
                otherPos?.q == pos.q && otherPos?.r == pos.r
            }

        // --- STEP 1: RESOLVE TROOP COLLISIONS FIRST ---
        val targetTroopEntity = otherEntities.find { it[TroopComponent.mapper] != null }
        var movingTroopSurvived = true

        if (targetTroopEntity != null) {
            val otherTroop = targetTroopEntity[TroopComponent.mapper]!!
            val otherTeam = targetTroopEntity[TeamComponent.mapper]?.team ?: TeamComponent.TeamName.NONE

            if (otherTeam == team.team) {
                // Friendly: Merge. returns true if fully merged (moving entity deleted)
                val fullyMerged = handleMerge(movingEntity, troop, targetTroopEntity, otherTroop)
                if (fullyMerged) movingTroopSurvived = false
            } else {
                // Enemy: Combat. returns true if moving troop won and stayed on tile
                movingTroopSurvived = handleCombat(movingEntity, troop, targetTroopEntity, otherTroop)
            }
        }

        // --- STEP 2: RESOLVE CITY CAPTURE ONLY IF TROOP SURVIVED ---
        if (movingTroopSurvived) {
            val cityEntity = otherEntities.find { it[CityComponent.mapper] != null }
            if (cityEntity != null) {
                val cityTeam = cityEntity[TeamComponent.mapper]
                // Capture if city is neutral or enemy
                if (cityTeam?.team != team.team) {
                    cityTeam?.team = team.team
                    println("City captured by ${team.team}")
                }
            }
        }

        troop.isColliding = false
    }

    /**
     * Returns true if movingEntity was removed (fully merged)
     */
    private fun handleMerge(movingEntity: Entity, movingTroop: TroopComponent, stationaryEntity: Entity, stationaryTroop: TroopComponent): Boolean {
        val total = movingTroop.strength + stationaryTroop.strength

        return if (total <= 99) {
            // Success: Combine all into the target tile
            stationaryTroop.strength = total
            engine.removeEntity(movingEntity)
            true
        } else {
            // Overflow: Target tile hits 99, moving troop bounces back with remainder
            stationaryTroop.strength = 99
            movingTroop.strength = total - 99

            val pos = movingEntity[PositionComponent.mapper]!!
            pos.q = pos.prevQ
            pos.r = pos.prevR
            false
        }
    }

    /**
     * Returns true if the moving troop survives the encounter
     */
    private fun handleCombat(movingEntity: Entity, movingTroop: TroopComponent, enemyEntity: Entity, enemyTroop: TroopComponent): Boolean {
        val diff = movingTroop.strength - enemyTroop.strength

        return when {
            diff > 0 -> { // Moving troop wins
                movingTroop.strength = diff
                engine.removeEntity(enemyEntity)
                true
            }
            diff < 0 -> { // Enemy troop wins (stationary wins)
                enemyTroop.strength = -diff
                engine.removeEntity(movingEntity)
                false
            }
            else -> { // Mutual destruction
                engine.removeEntity(movingEntity)
                engine.removeEntity(enemyEntity)
                false
            }
        }
    }
}
