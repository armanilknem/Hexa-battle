package com.tdt4240.group3.model

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.config.MapData
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.entities.CityConfig
import com.tdt4240.group3.model.entities.CityFactory
import com.tdt4240.group3.model.entities.TileConfig
import com.tdt4240.group3.model.entities.TileFactory
import com.tdt4240.group3.model.entities.TroopConfig
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.model.entities.CapitalFactory
import com.tdt4240.group3.model.hexmap.MapCalculations
import kotlin.math.floor
import kotlin.random.Random
import ktx.ashley.allOf
import ktx.ashley.get

class MapGenerator(private val engine: Engine) {

    private val tileFactory    = TileFactory(engine)
    private val troopFactory   = TroopFactory(engine)
    private val cityFactory    = CityFactory(engine)
    private val capitalFactory = CapitalFactory(engine)

    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()

    fun generateRectangularGrid(width: Int, height: Int) {
        for (r in 0 until height) {
            val rOffset = floor(r / 2.0).toInt()
            for (q in -rOffset until width - rOffset) {
                val type = if (q to r in MapData.WATER_TILES) TileComponent.TileType.WATER
                           else TileComponent.TileType.GRASS
                tileFactory.createEntity(TileConfig(q, r, type))
            }
        }
    }

    fun createTroopFromCity(cityEntity: Entity): Entity {
        val city     = cityEntity[CityComponent.mapper]!!
        val position = cityEntity[PositionComponent.mapper]!!
        val team     = cityEntity[TeamComponent.mapper]!!
        return troopFactory.createEntity(
            TroopConfig(
                team     = team.team,
                strength = city.baseProduction,
                q        = position.q,
                r        = position.r
            )
        )
    }

    fun generateCities(count: Int, capitalPositions: List<Pair<Int, Int>>, randomSeed: Int) {
        val cityNames = MapData.CITY_NAMES.toMutableList()
            .also { it.shuffle(Random(randomSeed)) }

        val candidateTiles = engine.getEntitiesFor(tileFamily).mapNotNull { entity ->
            if (entity[TileComponent.mapper]!!.type == TileComponent.TileType.WATER) return@mapNotNull null
            val pos = entity[PositionComponent.mapper]!!
            pos.q to pos.r
        }.filter { it !in capitalPositions }

        val placedCities = mutableListOf<Pair<Int, Int>>()

        for (tile in candidateTiles.shuffled(Random(randomSeed))) {
            if (placedCities.size >= count) break
            val tooClose = placedCities.any { placed ->
                MapCalculations.hexDistance(placed.first, placed.second, tile.first, tile.second) < 2
            } || capitalPositions.any { cap ->
                MapCalculations.hexDistance(cap.first, cap.second, tile.first, tile.second) < 2
            }
            if (!tooClose) {
                placedCities.add(tile)
                val name = cityNames.getOrElse(placedCities.size - 1) { "City ${placedCities.size}" }
                cityFactory.createEntity(CityConfig(
                    name           = name,
                    baseProduction = GameConstants.CITY_PRODUCTION,
                    q              = tile.first,
                    r              = tile.second,
                    team           = Team.NONE
                ))
            }
        }
    }

    fun generateCapitals(teams: List<Team>, randomSeed: Int): List<Pair<Int, Int>> {
        val capitalNames = MapData.CAPITAL_NAMES.toMutableList()
            .also { it.shuffle(Random(randomSeed)) }

        data class TilePos(val q: Int, val r: Int, val x: Float, val y: Float)

        val validTiles = engine.getEntitiesFor(tileFamily).mapNotNull { entity ->
            if (entity[TileComponent.mapper]!!.type == TileComponent.TileType.WATER) return@mapNotNull null
            val pos = entity[PositionComponent.mapper]!!
            TilePos(pos.q, pos.r, pos.x, pos.y)
        }

        if (validTiles.isEmpty()) return emptyList()

        val minX = validTiles.minOf { it.x }
        val maxX = validTiles.maxOf { it.x }
        val minY = validTiles.minOf { it.y }
        val maxY = validTiles.maxOf { it.y }

        val centerX = (minX + maxX) / 2f
        val padX = (maxX - minX) * GameConstants.CAPITAL_PADDING_FACTOR
        val padY = (maxY - minY) * GameConstants.CAPITAL_PADDING_FACTOR

        val anchors = listOf(
            (minX + padX) to (minY + padY), // bottom-left
            centerX to (minY + padY),       // bottom
            (maxX - padX) to (minY + padY), // bottom-right
            (maxX - padX) to (maxY - padY), // top-right
            centerX to (maxY - padY),       // top
            (minX + padX) to (maxY - padY)  // top-left
        )

        val anchorIndices = when (teams.size) {
            1    -> listOf(0)
            2    -> listOf(0, 3)
            3    -> listOf(0, 2, 4)
            4    -> listOf(0, 2, 3, 5)
            5    -> listOf(0, 1, 2, 3, 5)
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

            capitalFactory.createEntity(CityConfig(
                name           = capitalNames.getOrElse(index) { "Capital ${index + 1}" },
                baseProduction = GameConstants.CAPITAL_PRODUCTION,
                q              = bestTile.q,
                r              = bestTile.r,
                team           = team
            ))
        }

        return placedCapitals
    }
}
