package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class TerritorySystem : EntitySystem() {

    private val tileFamily = allOf(PositionComponent::class, TileComponent::class, TeamComponent::class).get()

    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()

    private val cityFamily = allOf(PositionComponent::class, CityComponent::class, TeamComponent::class).get()

    fun claimTerritory(centerTile: Entity, team: TeamComponent.TeamName) {
        val centerPos = centerTile[PositionComponent.mapper] ?: return

        claimCenterTile(centerTile, centerPos.q, centerPos.r, team)
        claimNearbyTiles(centerPos.q, centerPos.r, team)
    }

    private fun claimCenterTile(tile: Entity, q: Int, r: Int, team: TeamComponent.TeamName) {
        tile[TeamComponent.mapper]?.team = team
        findCityAt(q, r)?.get(TeamComponent.mapper)?.team = team
    }

    private fun claimNearbyTiles(centerQ: Int, centerR: Int, team: TeamComponent.TeamName) {
        val tiles = engine.entities.filter { tileFamily.matches(it) }

        tiles.forEach { tile ->
            val pos = tile[PositionComponent.mapper] ?: return@forEach

            if (pos.q == centerQ && pos.r == centerR) return@forEach
            if (!isWithinClaimRange(centerQ, centerR, pos.q, pos.r)) return@forEach
            if (!isClaimableTile(pos.q, pos.r)) return@forEach

            tile[TeamComponent.mapper]?.team = team
        }
    }

    private fun isWithinClaimRange(centerQ: Int, centerR: Int, q: Int, r: Int): Boolean {
        return hexDistance(centerQ, centerR, q, r) <= 1
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (Math.abs(q1 - q2) +
            Math.abs(q1 + r1 - q2 - r2) +
            Math.abs(r1 - r2)) / 2
    } // duplikat:((

    private fun isClaimableTile(q: Int, r: Int): Boolean {
        return !hasCityAt(q, r) && !hasTroopAt(q, r)
    }

    private fun hasCityAt(q: Int, r: Int): Boolean {
        return findCityAt(q, r) != null
    }

    private fun hasTroopAt(q: Int, r: Int): Boolean {
        return engine.entities.any { entity ->
            troopFamily.matches(entity) &&
                entity[PositionComponent.mapper]?.q == q &&
                entity[PositionComponent.mapper]?.r == r
        }
    }

    private fun findCityAt(q: Int, r: Int): Entity? {
        return engine.entities.firstOrNull { entity ->
            cityFamily.matches(entity) &&
                entity[PositionComponent.mapper]?.q == q &&
                entity[PositionComponent.mapper]?.r == r
        }
    }
}
