package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.entities.unit.UnitCatalog
import com.tdt4240.group3.model.components.CombatComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.MovementComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.UnitComponent
import ktx.ashley.entity
import ktx.ashley.with
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

class EntityFactory(private val engine: Engine) {
    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent > {
            this.name = name
        }
    }

    fun generateRectangularGrid(width: Int, height: Int) {
        for (r in 0 until height) {
            val rOffset = floor(r / 2.0).toInt()
            for (q in -rOffset until width - rOffset) {
                createTile(q, r, TileComponent.TileType.GRASS)
            }
        }
    }

    /**
     * Creates a troop entity based on a definition from the UnitCatalog.
     */
    fun createTroop(team: TeamComponent.TeamName, unitKey: String, strength: Int, q: Int, r: Int) = engine.entity {
        val unitDef = UnitCatalog.units.getValue(unitKey)
        with<UnitComponent> {
            this.unitKey = unitDef.key
        }
        with<MovementComponent> {
            this.moveRange = unitDef.movement.moveRange
            this.canCrossWater = unitDef.movement.canCrossWater
        }
        with<CombatComponent> {
            this.maxStackSize = unitDef.combat.maxStackSize
            this.attackMultiplier = unitDef.combat.attackMultiplier
            this.defenseMultiplier = unitDef.combat.defenseMultiplier
            this.canMergeFriendly = unitDef.combat.canMergeFriendly
        }
        with<TroopComponent> {
            this.strength = strength
        }
        with<PositionComponent> {
            this.q = q
            this.r = r
            this.zIndex = 2 // Top layer
        }
        with<TeamComponent> {
            this.team = team
        }
    }
    fun createCity(name: String, isCapital: Boolean, baseProduction: Int, q: Int, r: Int, team: TeamComponent.TeamName) = engine.entity {
        with<CityComponent> {
            this.name = name
            this.baseProduction = baseProduction
            this.isCapital = isCapital
        }
        with<PositionComponent> {
            this.q = q
            this.r = r
            this.zIndex = 1 // Middle layer
        }
        with<TeamComponent> {
            this.team = team
        }
    }

    /**
     * Creates a troop entity from a city entity based on definition from the UnitCatalog.
     */
    fun createTroopFromCity(cityEntity: Entity, troopType: String): Entity {
        val city = CityComponent.mapper.get(cityEntity)
        val position = PositionComponent.mapper.get(cityEntity)
        val team = TeamComponent.mapper.get(cityEntity)
        return createTroop(team.team, troopType, city.baseProduction, position.q, position.r)
    }

    fun createTile(q: Int, r: Int, type: TileComponent.TileType): Entity = engine.entity {
        with<TileComponent> {
            this.type = type
        }
        with<PositionComponent> {
            this.q = q
            this.r = r
            this.zIndex = 0 // Bottom layer
        }
        with<TeamComponent> {
            this.team = TeamComponent.TeamName.NONE
        }
    }

    fun createGameState() = engine.entity {
        with<GameStateComponent>()
    }

    fun generateNormalCities(count: Int, capitalPositions: List<Pair<Int, Int>>) {
        val cityNames = MapData.CITY_NAMES.toMutableList()
            .also { it.shuffle(Random(System.currentTimeMillis())) }

        val tileFamily = ktx.ashley.allOf(PositionComponent::class, TileComponent::class).get()
        val allTiles = engine.getEntitiesFor(tileFamily).map { entity ->
            val pos = PositionComponent.mapper.get(entity)
            Pair(pos.q, pos.r)
        }

        val candidateTiles = allTiles.filter { it !in MapData.WATER_TILES && it !in capitalPositions }

        val placedCities = mutableListOf<Pair<Int, Int>>()

        val shuffled = candidateTiles.shuffled(Random(System.currentTimeMillis()))
        for (tile in shuffled) {
            if (placedCities.size >= count) break
            val tooClose = placedCities.any { placed ->
                hexDistance(placed.first, placed.second, tile.first, tile.second) < 2
            } || capitalPositions.any { cap ->
                hexDistance(cap.first, cap.second, tile.first, tile.second) < 2
            }
            if (!tooClose) {
                placedCities.add(tile)
                val name = cityNames.getOrElse(placedCities.size - 1) { "City ${placedCities.size}" }
                createCity(
                    name = name,
                    isCapital = false,
                    baseProduction = 10,
                    q = tile.first,
                    r = tile.second,
                    team = TeamComponent.TeamName.NONE
                )
            }
        }
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (abs(q1 - q2) + abs(q1 + r1 - q2 - r2) + abs(r1 - r2)) / 2
    }
}
