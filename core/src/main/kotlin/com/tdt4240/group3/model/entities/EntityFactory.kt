package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.Team
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.*
import com.badlogic.ashley.core.Entity
import ktx.ashley.entity
import ktx.ashley.with


class EntityFactory(private val engine: Engine) {

    // Method to create the entire grid at once
    fun generateRectangularGrid(width: Int, height: Int) {
        for (r in 0 until height) {
            val rOffset = Math.floor(r / 2.0).toInt()
            for (q in -rOffset until width - rOffset) {
                createTile(q, r, TileComponent.TileType.GRASS)
            }
        }
    }

    fun createTile(q: Int, r: Int, type: TileComponent.TileType): Entity = engine.entity {
        with<HexComponent> {
            this.q = q
            this.r = r
        }
        with<TileComponent> {
            this.type = type
        }
        with<PositionComponent> {
            // We convert Hex(q,r) to Pixel(x,y) here for the View to use
            val coords = hexToPixel(q, r)
            this.x = coords.first.toInt()
            this.y = coords.second.toInt()
            this.zIndex = 0 // Bottom layer
        }
    }

    fun createCity(
        name: String,
        isCapital: Boolean,
        baseProduction: Int,
        q: Int,
        r: Int,
        team: TeamComponent.TeamName
    ): Entity = engine.entity {
        with<HexComponent> {
            this.q = q; this.r = r
        }
        with<CityComponent> {
            this.name = name
            this.isCapital = isCapital
            this.baseProduction = baseProduction
        }
        with<PositionComponent> {
            val coords = hexToPixel(q, r)
            this.x = coords.first.toInt()
            this.y = coords.second.toInt()
            this.zIndex = 1 // Middle layer
        }
        with<TeamComponent> {
            this.team = team
        }
    }

    fun createTroop(q: Int, r: Int, team: Team, strength: Int): Entity = engine.entity {
        with<HexComponent> { this.q = q; this.r = r }
        with<TroopsComponent> {
            this.team = team
            this.strength = strength
        }
        with<PositionComponent> {
            val coords = hexToPixel(q, r)
            this.x = coords.first.toInt()
            this.y = coords.second.toInt()
            this.zIndex = 2 // Top layer
        }
    }

    private fun hexToPixel(q: Int, r: Int): Pair<Float, Float> {
        val size = 32f // The radius of your hex sprite
        val x = size * (Math.sqrt(3.0).toFloat() * q + Math.sqrt(3.0).toFloat() / 2f * r)
        val y = size * (3f / 2f * r)
        return Pair(x, y)
    }

    fun createPlayer(name: String) = engine.entity {
        with<PlayerComponent > {
            this.name = name
        }
    }
}
