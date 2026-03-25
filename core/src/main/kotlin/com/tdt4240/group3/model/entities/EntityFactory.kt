package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.tdt4240.group3.model.components.HexComponent
import com.tdt4240.group3.model.components.TileComponent
import ktx.ashley.entity
import ktx.ashley.with
import kotlin.math.floor
import kotlin.math.sqrt

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

    fun createTroop( team: TeamComponent.TeamName, strength: Int, q: Int, r: Int) = engine.entity {
        with<HexComponent> { this.q = q; this.r = r }
        with<TroopComponent> {
            this.strength = strength
            this.isMoved = false
            this.isClicked = false
        }
        with<PositionComponent> {
            val coords = hexToPixel(q, r)
            this.x = coords.first.toInt()
            this.y = coords.second.toInt()
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
            val coords = hexToPixel(q, r)
            this.x = coords.first.toInt()
            this.y = coords.second.toInt()
            this.zIndex = 1 // Middle layer
        }
        with<TeamComponent> {
            this.team = team
        }
    }
    private fun hexToPixel(q: Int, r: Int): Pair<Float, Float> {
        val size = 32f // The radius of your hex sprite
        val x = size * (sqrt(3.0).toFloat() * q + sqrt(3.0).toFloat() / 2f * r)
        val y = size * (3f / 2f * r)
        return Pair(x, y)
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
}
