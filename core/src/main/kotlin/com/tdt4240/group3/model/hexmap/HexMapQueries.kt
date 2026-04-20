package com.tdt4240.group3.model.hexmap

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

object HexMapQueries {
    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val cityFamily = allOf(PositionComponent::class, CityComponent::class).get()

    fun findTileAt(engine: Engine, worldX: Float, worldY: Float): Entity? {
        val entities = engine.getEntitiesFor(tileFamily)
        if (entities.size() == 0) return null

        return entities.minByOrNull { distSq(it, worldX, worldY) }
            ?.takeIf { distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ }
    }

    fun findTroopAt(engine: Engine, worldX: Float, worldY: Float, exclude: Entity? = null): Entity? {
        return engine.getEntitiesFor(troopFamily).firstOrNull {
            it != exclude && distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ
        }
    }

    fun findCityAtByXY(engine: Engine, worldX: Float, worldY: Float): Entity? {
        return engine.getEntitiesFor(cityFamily).firstOrNull {
            distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ
        }
    }

    fun findCityAtByQR(engine: Engine, q: Int, r: Int): Entity? {
        return engine.entities.firstOrNull { entity ->
            cityFamily.matches(entity) &&
                entity[PositionComponent.mapper]?.q == q &&
                entity[PositionComponent.mapper]?.r == r
        }
    }

    fun hasTroopAt(engine: Engine, q: Int, r: Int): Boolean {
        return engine.entities.any { entity ->
            troopFamily.matches(entity) &&
                entity[PositionComponent.mapper]?.q == q &&
                entity[PositionComponent.mapper]?.r == r
        }
    }

    fun hasCityAt(engine: Engine, q: Int, r: Int): Boolean {
        return findCityAtByQR(engine, q, r) != null
    }

    private fun distSq(entity: Entity, x: Float, y: Float): Float {
        val pos = entity[PositionComponent.mapper] ?: return Float.MAX_VALUE
        val dx = pos.x - x
        val dy = pos.y - y
        return dx * dx + dy * dy
    }
}
