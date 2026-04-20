package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.marker.TerritoryComponent
import com.tdt4240.group3.model.hexmap.HexMapQueries.findCityAtByQR
import com.tdt4240.group3.model.hexmap.HexMapQueries.hasCityAt
import com.tdt4240.group3.model.hexmap.HexMapQueries.hasTroopAt
import com.tdt4240.group3.model.hexmap.MapCalculations.hexDistance
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.model.LobbyMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get

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

    fun claimTerritory(centerTile: Entity, team: Team) {
        val centerPos = centerTile[PositionComponent.mapper] ?: return

        claimCenterTile(centerTile, centerPos.q, centerPos.r, team)
        claimNearbyTiles(centerPos.q, centerPos.r, team)
    }

    private fun claimCenterTile(tile: Entity, q: Int, r: Int, team: Team) {
        val tileAtCenter = engine.entities.firstOrNull {
            tileFamily.matches(it) &&
                it[PositionComponent.mapper]?.q == q &&
                it[PositionComponent.mapper]?.r == r
        }

        tileAtCenter?.get(TeamComponent.mapper)?.team = team
        findCityAtByQR(engine, q, r)?.get(TeamComponent.mapper)?.team = team
    }

    private fun claimNearbyTiles(centerQ: Int, centerR: Int, team: Team) {
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

    private fun isClaimableTile(q: Int, r: Int): Boolean {
        return !hasCityAt(engine, q, r) && !hasTroopAt(engine, q, r)
    }
}
