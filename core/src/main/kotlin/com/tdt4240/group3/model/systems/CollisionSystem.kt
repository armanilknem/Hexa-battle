package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.components.CombatComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.CollidingComponent
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.marker.TerritoryComponent
import com.tdt4240.group3.model.Team
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.ceil
import kotlin.math.max

class CollisionSystem : IteratingSystem(
    allOf(
        TroopComponent::class,
        CombatComponent::class,
        PositionComponent::class,
        TeamComponent::class,
        CollidingComponent::class
    ).get()
) {

    override fun processEntity(movingEntity: Entity, deltaTime: Float) {
        val troop = movingEntity[TroopComponent.mapper]!!
        val combat = movingEntity[CombatComponent.mapper] ?: return
        val pos = movingEntity[PositionComponent.mapper]!!
        val team = movingEntity[TeamComponent.mapper]!!

        val otherEntities = engine.getEntitiesFor(allOf(PositionComponent::class).get())
            .filter { it != movingEntity }
            .filter {
                val otherPos = it[PositionComponent.mapper]
                otherPos?.q == pos.q && otherPos?.r == pos.r
            }

        val targetTroopEntity = otherEntities.find {
            it[TroopComponent.mapper] != null && it[CombatComponent.mapper] != null
        }
        var movingTroopSurvived = true

        if (targetTroopEntity != null) {
            val otherTroop = targetTroopEntity[TroopComponent.mapper]!!
            val otherPosition = targetTroopEntity[PositionComponent.mapper]!!
            val otherCombat = targetTroopEntity[CombatComponent.mapper] ?: return
            val otherTeam = targetTroopEntity[TeamComponent.mapper]?.team ?: Team.NONE

            if (otherTeam == team.team) {
                val fullyMerged = handleMerge(movingEntity, troop, combat, targetTroopEntity, otherTroop, otherCombat)
                if (fullyMerged) movingTroopSurvived = false
            } else {
                movingTroopSurvived = handleCombat(movingEntity, pos, troop, combat, targetTroopEntity, otherPosition, otherTroop, otherCombat)
            }
        }

        if (movingTroopSurvived) {
            val cityEntity = otherEntities.find { it[CityComponent.mapper] != null }
            if (cityEntity != null) {
                val cityTeam = cityEntity[TeamComponent.mapper]
                if (cityTeam?.team != team.team) {
                    cityTeam?.team = team.team
                }
            }
        }

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

        val movingPos = movingEntity[PositionComponent.mapper]!!
        val stationaryPos = stationaryEntity[PositionComponent.mapper]!!

        val total = movingTroop.strength + stationaryTroop.strength
        val maxStack = stationaryCombat.maxStackSize

        return if (total <= maxStack) {
            stationaryPos.prevQ = movingPos.prevQ
            stationaryPos.prevR = movingPos.prevR
            stationaryTroop.strength = total
            stationaryEntity.add(TerritoryComponent())
            engine.removeEntity(movingEntity)
            true
        } else {
            stationaryTroop.strength = maxStack
            movingTroop.strength = total - maxStack

            val oldQ = movingPos.q
            val oldR = movingPos.r
            bounceBack(movingEntity)
            movingPos.prevQ = oldQ
            movingPos.prevR = oldR
            false
        }
    }

    private fun handleCombat(
        movingEntity: Entity,
        movingPosition: PositionComponent,
        movingTroop: TroopComponent,
        movingCombat: CombatComponent,
        enemyEntity: Entity,
        enemyPosition: PositionComponent,
        enemyTroop: TroopComponent,
        enemyCombat: CombatComponent
    ): Boolean {
        val attackerPower = movingTroop.strength * movingCombat.attackMultiplier
        val defenderPower = enemyTroop.strength * enemyCombat.defenseMultiplier
        val diff = attackerPower - defenderPower

        return when {
            diff > 0 -> {
                movingTroop.strength = max(1, ceil(diff / movingCombat.attackMultiplier).toInt())
                engine.removeEntity(enemyEntity)
                true
            }
            diff < 0 -> {
                enemyTroop.strength = max(1, ceil((-diff) / enemyCombat.defenseMultiplier).toInt())
                engine.removeEntity(movingEntity)
                enemyPosition.prevQ = movingPosition.prevQ
                enemyPosition.prevR = movingPosition.prevR
                enemyEntity.add(TerritoryComponent())
                false
            }
            else -> {
                claimTileAt(movingPosition.prevQ, movingPosition.prevR)
                claimTileAt(enemyPosition.q, enemyPosition.r)
                engine.removeEntity(movingEntity)
                engine.removeEntity(enemyEntity)
                false
            }
        }
    }

    private fun claimTileAt(q: Int, r: Int) {
        for (entity in engine.entities) {
            val p = entity[PositionComponent.mapper] ?: continue
            if (p.q == q && p.r == r) {
                entity.add(TerritoryComponent())
                break
            }
        }
    }

    private fun bounceBack(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        pos.q = pos.prevQ
        pos.r = pos.prevR
    }
}
