package com.tdt4240.group3.model.ecs.entities

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.team.TeamName
import com.tdt4240.group3.model.ecs.components.CityComponent
import com.tdt4240.group3.model.ecs.components.CombatComponent
import com.tdt4240.group3.model.ecs.components.GameStateComponent
import com.tdt4240.group3.model.ecs.components.MovementComponent
import com.tdt4240.group3.model.ecs.components.PlayerComponent
import com.tdt4240.group3.model.ecs.components.PositionComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.components.TileComponent
import com.tdt4240.group3.model.ecs.components.TroopComponent
import com.tdt4240.group3.model.ecs.components.UnitComponent
import com.tdt4240.group3.model.map.MapData
import com.tdt4240.group3.model.unit.UnitCatalog
import ktx.ashley.entity
import ktx.ashley.with
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

class EntityFactory(private val engine: Engine) {
    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent> {
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
    fun createTroop(team: TeamName, unitKey: String, strength: Int, q: Int, r: Int) = engine.entity {
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
    fun createCity(name: String, baseProduction: Int, q: Int, r: Int, team: TeamName) = engine.entity {
        with<CityComponent> {
            this.name = name
            this.baseProduction = baseProduction
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

    fun createCapital(name: String, baseProduction: Int, q: Int, r: Int, team: TeamName) = engine.entity {
        with<CityComponent> {
            this.name = name
            this.baseProduction = baseProduction
        }
        with<PositionComponent> {
            this.q = q
            this.r = r
            this.zIndex = 1 // Middle layer
        }
        with<TeamComponent> {
            this.team = team
        }
        with<CapitalComponent> {}
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
            this.team = TeamName.NONE
        }
    }

    fun createGameState(activeTeams: List<TeamName>) = engine.entity {
        with<GameStateComponent> {
            initialize(activeTeams)
        }
    }

    fun generateCapitals(teams: List<TeamName>): List<Pair<Int, Int>> {
        val capitalNames = MapData.CAPITAL_NAMES.toMutableList()
            .also { it.shuffle(Random(System.currentTimeMillis())) }

        val tileFamily = ktx.ashley.allOf(PositionComponent::class, TileComponent::class).get()

        data class TilePos(val q: Int, val r: Int, val x: Float, val y: Float)

        val validTiles = engine.getEntitiesFor(tileFamily)
            .mapNotNull { entity ->
                val pos = PositionComponent.mapper.get(entity) ?: return@mapNotNull null
                val coords = pos.q to pos.r
                if (coords in MapData.WATER_TILES) return@mapNotNull null
                TilePos(pos.q, pos.r, pos.x, pos.y)
            }

        if (validTiles.isEmpty()) return emptyList()

        val minX = validTiles.minOf { it.x }
        val maxX = validTiles.maxOf { it.x }
        val minY = validTiles.minOf { it.y }
        val maxY = validTiles.maxOf { it.y }

        val centerX = (minX + maxX) / 2f
        val padX = (maxX - minX) * 0.12f
        val padY = (maxY - minY) * 0.12f

        val anchors = listOf(
            (minX + padX) to (minY + padY), // top-left-ish
            centerX to (minY + padY),       // top
            (maxX - padX) to (minY + padY), // top-right-ish
            (maxX - padX) to (maxY - padY), // bottom-right-ish
            centerX to (maxY - padY),       // bottom
            (minX + padX) to (maxY - padY)  // bottom-left-ish
        )

        val anchorIndices = when (teams.size) {
            1 -> listOf(0)
            2 -> listOf(0, 3)
            3 -> listOf(0, 2, 4)
            4 -> listOf(0, 1, 3, 5)
            5 -> listOf(0, 1, 2, 3, 5)
            else -> listOf(0, 1, 2, 3, 4, 5)
        }

        val used = mutableSetOf<Pair<Int, Int>>()
        val placedCapitals = mutableListOf<Pair<Int, Int>>()

        teams.take(anchorIndices.size).forEachIndexed { index, team ->
            val (ax, ay) = anchors[anchorIndices[index]]

            val bestTile = validTiles
                .filter { (it.q to it.r) !in used }
                .minByOrNull { tile ->
                    val dx = tile.x - ax
                    val dy = tile.y - ay
                    dx * dx + dy * dy
                } ?: return@forEachIndexed

            val coords = bestTile.q to bestTile.r
            used.add(coords)
            placedCapitals.add(coords)

            createCapital(
                name = capitalNames.getOrElse(index) { "Capital ${index + 1}" },
                baseProduction = 20,
                q = bestTile.q,
                r = bestTile.r,
                team = team
            )
        }

        return placedCapitals
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
                    baseProduction = 10,
                    q = tile.first,
                    r = tile.second,
                    team = TeamName.NONE
                )
            }
        }
    }



    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (abs(q1 - q2) + abs(q1 + r1 - q2 - r2) + abs(r1 - r2)) / 2
    }
}
