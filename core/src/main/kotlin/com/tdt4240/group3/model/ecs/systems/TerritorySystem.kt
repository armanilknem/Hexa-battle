package com.tdt4240.group3.model.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.components.marker.*
import com.tdt4240.group3.model.team.TeamName
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.model.LobbyMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get
import javax.swing.text.Position

class TerritorySystem(private val lobbyId: Int) : IteratingSystem(
        allOf(PositionComponent::class,
        TeamComponent::class,
        TerritoryComponent::class
    ).get()
) {
    private val tileFamily = allOf(PositionComponent::class, TileComponent::class, TeamComponent::class).get()

    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()

    private val cityFamily = allOf(PositionComponent::class, CityComponent::class, TeamComponent::class).get()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val team = entity[TeamComponent.mapper] ?: return
        val pos = entity[PositionComponent.mapper] ?: return
        this.claimTerritory(entity, team.team)

        val updatedLobbyMapStates = mutableListOf<LobbyMapState>()
        val updatedPositions = listOf(
            Pair(pos.q, pos.r),
            Pair(pos.prevQ, pos.prevR),
            Pair(pos.q + 1, pos.r),
            Pair(pos.q + 1, pos.r - 1),
            Pair(pos.q, pos.r - 1),
            Pair(pos.q - 1, pos.r),
            Pair(pos.q - 1, pos.r + 1),
            Pair(pos.q, pos.r + 1)
        ).distinct()

        val gameStateEntity = engine.getEntitiesFor(
            allOf(GameStateComponent::class).get()
        ).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper]!!

        println("Territory system running")
        println("Player order: " + gs.playerOrder)
        println("Current player: " + gs.playerOrder[gs.activeTeams.indexOf(team.team)])


        for (position in updatedPositions) {
            val entity = findEntityAt(position.first, position.second) ?: continue
            val troopComp = entity[TroopComponent.mapper]
            val teamComponent = entity[TeamComponent.mapper]!!

            val idx = gs.activeTeams.indexOf(teamComponent.team)
            val ownerId = if (idx != -1) gs.playerOrder[idx] else null

            updatedLobbyMapStates.add(
                LobbyMapState(
                    lobbyId=lobbyId,
                    q=position.first,
                    r=position.second,
                    ownerId=ownerId,
                    strength=troopComp?.strength ?: 0
                )
            )
        }

        scope.launch {
            LobbyGameStateService.setLobbyMapStates(updatedLobbyMapStates)
        }

        entity.remove(TerritoryComponent::class.java)
    }

    fun findEntityAt(q: Int, r: Int): Entity? {
        engine.getEntitiesFor(troopFamily)
            .find { it[PositionComponent.mapper]!!.q == q && it[PositionComponent.mapper]!!.r == r }
            ?.let { return it }

        engine.getEntitiesFor(cityFamily)
            .find { it[PositionComponent.mapper]!!.q == q && it[PositionComponent.mapper]!!.r == r }
            ?.let { return it }

        engine.getEntitiesFor(tileFamily)
            .find { it[PositionComponent.mapper]!!.q == q && it[PositionComponent.mapper]!!.r == r }
            ?.let { return it }

        return null
    }


    fun claimTerritory(centerTile: Entity, team: TeamName) {
        val centerPos = centerTile[PositionComponent.mapper] ?: return

        claimCenterTile(centerTile, centerPos.q, centerPos.r, team)
        claimNearbyTiles(centerPos.q, centerPos.r, team)
    }

    private fun claimCenterTile(tile: Entity, q: Int, r: Int, team: TeamName) {
        val tileAtCenter = engine.entities.firstOrNull {
            tileFamily.matches(it) &&
                it[PositionComponent.mapper]?.q == q &&
                it[PositionComponent.mapper]?.r == r
        }

        tileAtCenter?.get(TeamComponent.mapper)?.team = team
        findCityAt(q, r)?.get(TeamComponent.mapper)?.team = team
    }

    private fun claimNearbyTiles(centerQ: Int, centerR: Int, team: TeamName) {
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
