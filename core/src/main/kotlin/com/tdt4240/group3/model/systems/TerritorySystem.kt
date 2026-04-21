package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.TerritoryComponent
import com.tdt4240.group3.model.hexmap.HexMapQueries.findCityAtByQR
import com.tdt4240.group3.model.hexmap.HexMapQueries.hasCityAt
import com.tdt4240.group3.model.hexmap.HexMapQueries.hasTroopAt
import com.tdt4240.group3.model.hexmap.MapCalculations.hexDistance
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.model.LobbyMapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Processes [TerritoryComponent] markers each frame.
 * For each entity carrying the marker, claims the surrounding hex tiles for the entity's team,
 * then syncs affected map positions to Supabase and removes the marker.
 */
class TerritorySystem(private val lobbyId: Int) : IteratingSystem(
    allOf(
        PositionComponent::class,
        TeamComponent::class,
        TerritoryComponent::class
    ).get()
) {
    private val tileFamily      = allOf(PositionComponent::class, TileComponent::class, TeamComponent::class).get()
    private val troopFamily     = allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()
    private val cityFamily      = allOf(PositionComponent::class, CityComponent::class, TeamComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class).get()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val team = entity[TeamComponent.mapper]  ?: return
        val pos  = entity[PositionComponent.mapper] ?: return

        claimTerritory(entity, team.team)

        val updatedPositions = listOf(
            pos.q      to pos.r,
            pos.prevQ  to pos.prevR,
            pos.q + 1  to pos.r,
            pos.q + 1  to pos.r - 1,
            pos.q      to pos.r - 1,
            pos.q - 1  to pos.r,
            pos.q - 1  to pos.r + 1,
            pos.q      to pos.r + 1
        ).distinct()

        val gs = engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper) ?: return

        val updatedLobbyMapStates = updatedPositions.mapNotNull { (q, r) ->
            val target = findEntityAt(q, r) ?: return@mapNotNull null
            val teamComponent = target[TeamComponent.mapper]!!
            val idx = gs.activeTeams.indexOf(teamComponent.team)
            val ownerId = if (idx != -1) gs.playerOrder[idx] else null
            LobbyMapState(
                lobbyId  = lobbyId,
                q        = q,
                r        = r,
                ownerId  = ownerId,
                strength = target[TroopComponent.mapper]?.strength ?: 0
            )
        }

        scope.launch { LobbyGameStateService.setLobbyMapStates(updatedLobbyMapStates) }
        entity.remove(TerritoryComponent::class.java)
    }

    fun claimTerritory(centerTile: Entity, team: Team) {
        val centerPos = centerTile[PositionComponent.mapper] ?: return
        claimCenterTile(centerPos.q, centerPos.r, team)
        claimNearbyTiles(centerPos.q, centerPos.r, team)
    }

    private fun claimCenterTile(q: Int, r: Int, team: Team) {
        engine.getEntitiesFor(tileFamily).firstOrNull { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }?.get(TeamComponent.mapper)?.team = team
        findCityAtByQR(engine, q, r)?.get(TeamComponent.mapper)?.team = team
    }

    private fun claimNearbyTiles(centerQ: Int, centerR: Int, team: Team) {
        engine.getEntitiesFor(tileFamily).forEach { tile ->
            val pos = tile[PositionComponent.mapper] ?: return@forEach
            if (pos.q == centerQ && pos.r == centerR) return@forEach
            if (hexDistance(centerQ, centerR, pos.q, pos.r) > 1) return@forEach
            if (!isClaimableTile(pos.q, pos.r)) return@forEach
            tile[TeamComponent.mapper]?.team = team
        }
    }

    private fun isClaimableTile(q: Int, r: Int): Boolean =
        !hasCityAt(engine, q, r) && !hasTroopAt(engine, q, r)

    private fun findEntityAt(q: Int, r: Int): Entity? {
        engine.getEntitiesFor(troopFamily).find { entity ->
            val pos = entity[PositionComponent.mapper]!!
            pos.q == q && pos.r == r
        }?.let { return it }

        engine.getEntitiesFor(cityFamily).find { entity ->
            val pos = entity[PositionComponent.mapper]!!
            pos.q == q && pos.r == r
        }?.let { return it }

        return engine.getEntitiesFor(tileFamily).find { entity ->
            val pos = entity[PositionComponent.mapper]!!
            pos.q == q && pos.r == r
        }
    }
}
