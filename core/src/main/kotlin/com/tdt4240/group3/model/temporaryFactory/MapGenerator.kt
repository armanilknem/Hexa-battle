package com.tdt4240.group3.model.temporaryFactory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.MapData
import com.tdt4240.group3.model.ecs.components.CityComponent
import com.tdt4240.group3.model.ecs.components.PositionComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.components.TileComponent
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.allOf
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

class MapGenerator(private val engine: Engine) {

    private val tileFactory = TileFactory(engine)
    private val troopFactory = TroopFactory(engine)
    private val cityFactory = CityFactory(engine)
    private val capitalFactory = CapitalFactory(engine)

    fun generateRectangularGrid(width: Int, height: Int) {
        for (r in 0 until height) {
            val rOffset = floor(r / 2.0).toInt()
            for (q in -rOffset until width - rOffset) {
                tileFactory.createEntity(TileConfig(q, r, TileComponent.TileType.GRASS))
            }
        }
    }

    //TODO("This function should probably be in a different file, maybe TurnSystem")
    fun createTroopFromCity(cityEntity: Entity, troopType: String): Entity {
        val city = CityComponent.Companion.mapper.get(cityEntity)
        val position = PositionComponent.Companion.mapper.get(cityEntity)
        val team = TeamComponent.Companion.mapper.get(cityEntity)
        return troopFactory.createEntity(
            TroopConfig(
                team.team,
                troopType,
                city.baseProduction,
                position.q,
                position.r
            )
        )
    }

    //TODO("The variables should be changed for more clarity")
    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (abs(q1 - q2) + abs(q1 + r1 - q2 - r2) + abs(r1 - r2)) / 2
    }

    fun generateCities(count: Int, capitalPositions: List<Pair<Int, Int>>) {
        val cityNames = MapData.CITY_NAMES.toMutableList()
            .also { it.shuffle(Random(System.currentTimeMillis())) }

        val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()
        val allTiles = engine.getEntitiesFor(tileFamily).map { entity ->
            val pos = PositionComponent.Companion.mapper.get(entity)
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
                cityFactory.createEntity(
                    CityConfig(
                        name = name,
                        baseProduction = 10,
                        q = tile.first,
                        r = tile.second,
                        team = TeamName.NONE
                    )
                )
            }
        }
    }

    //TODO("Should be refactored for better clarity")
    fun generateCapitals(teams: List<TeamName>): List<Pair<Int, Int>> {
        val capitalNames = MapData.CAPITAL_NAMES.toMutableList()
            .also { it.shuffle(Random(System.currentTimeMillis())) }

        val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()

        data class TilePos(val q: Int, val r: Int, val x: Float, val y: Float)

        val validTiles = engine.getEntitiesFor(tileFamily)
            .mapNotNull { entity ->
                val pos = PositionComponent.Companion.mapper.get(entity) ?: return@mapNotNull null
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
        val padX = (maxX - minX) * 0.12f //TODO("Use of magic numbers should be minimized")
        val padY = (maxY - minY) * 0.12f

        val anchors = listOf(
            (minX + padX) to (minY + padY), // bottom-left
            centerX to (minY + padY),       // bottom
            (maxX - padX) to (minY + padY), // bottom-right
            (maxX - padX) to (maxY - padY), // top-right
            centerX to (maxY - padY),       // top
            (minX + padX) to (maxY - padY)  // top-left
        )

        val anchorIndices = when (teams.size) { //TODO("This should be more dynamic and not hardcoded for better modifiability")
            1 -> listOf(0)
            2 -> listOf(0, 3)
            3 -> listOf(0, 2, 4)
            4 -> listOf(0, 2, 3, 5)
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

            capitalFactory.createEntity(
                CapitalConfig(
                    name = capitalNames.getOrElse(index) { "Capital ${index + 1}" },
                    baseProduction = 20,
                    q = bestTile.q,
                    r = bestTile.r,
                    team = team
                )
            )
        }

        return placedCapitals
    }


}
