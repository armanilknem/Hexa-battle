package com.tdt4240.group3.model.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.ecs.components.CombatComponent
import com.tdt4240.group3.model.ecs.components.PositionComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.components.TroopComponent
import com.tdt4240.group3.model.ecs.components.marker.CollidingComponent
import com.tdt4240.group3.model.ecs.components.CityComponent
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.ceil
import kotlin.math.max

class CollisionSystem : IteratingSystem(allOf(
    TroopComponent::class,
    CombatComponent::class,
    PositionComponent::class,
    TeamComponent::class,
    CollidingComponent::class).get()){

    override fun processEntity(movingEntity: Entity, deltaTime: Float) {
        val troop = movingEntity[TroopComponent.mapper]!!
        val combat = movingEntity.getComponent(CombatComponent::class.java) ?: return
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
        val targetTroopEntity = otherEntities.find {
            it[TroopComponent.mapper] != null && it.getComponent(CombatComponent::class.java) != null
        }
        var movingTroopSurvived = true

        if (targetTroopEntity != null) {
            val otherTroop = targetTroopEntity[TroopComponent.mapper]!!
            val otherCombat = targetTroopEntity.getComponent(CombatComponent::class.java) ?: return
            val otherTeam = targetTroopEntity[TeamComponent.mapper]?.team ?: TeamName.NONE

            if (otherTeam == team.team) {
                // Friendly: Merge. returns true if fully merged (moving entity deleted)
                val fullyMerged = handleMerge(movingEntity, troop, combat, targetTroopEntity, otherTroop, otherCombat)
                if (fullyMerged) movingTroopSurvived = false
            } else {
                // Enemy: Combat. returns true if moving troop won and stayed on tile
                movingTroopSurvived = handleCombat(movingEntity, troop, combat, targetTroopEntity, otherTroop, otherCombat)
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

        // Remove collidingComponent
        movingEntity.remove(CollidingComponent::class.java)
    }

    private fun handleMerge(
        movingEntity: Entity,
        movingTroop: TroopComponent,
        movingCombat: CombatComponent,
        stationaryEntity: Entity,
        stationaryTroop: TroopComponent,
        stationaryCombat: CombatComponent
    ): Boolean {
        if (!movingCombat.canMergeFriendly || !stationaryCombat.canMergeFriendly) {
            bounceBack(movingEntity)
            return false
        }

        val total = movingTroop.strength + stationaryTroop.strength
        val maxStack = stationaryCombat.maxStackSize

        return if (total <= maxStack) {
            // Success: Combine all into the target tile
            stationaryTroop.strength = total
            engine.removeEntity(movingEntity)
            true
        } else {
            // Overflow: Target tile hits 99, moving troop bounces back with remainder
            stationaryTroop.strength = maxStack
            movingTroop.strength = total - maxStack
            bounceBack(movingEntity)
            false
        }
    }

    private fun handleCombat(
        movingEntity: Entity,
        movingTroop: TroopComponent,
        movingCombat: CombatComponent,
        enemyEntity: Entity,
        enemyTroop: TroopComponent,
        enemyCombat: CombatComponent
    ): Boolean {
        val attackerPower = movingTroop.strength * movingCombat.attackMultiplier
        val defenderPower = enemyTroop.strength * enemyCombat.defenseMultiplier
        val diff = attackerPower - defenderPower

        return when {
            diff > 0 -> { // Moving troop wins
                movingTroop.strength = max(1, ceil(diff / movingCombat.attackMultiplier).toInt())
                engine.removeEntity(enemyEntity)
                true
            }
            diff < 0 -> { // Enemy troop wins (stationary wins)
                enemyTroop.strength = max(1, ceil((-diff) / enemyCombat.defenseMultiplier).toInt())
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

    private fun bounceBack(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        pos.q = pos.prevQ
        pos.r = pos.prevR
    }
}
