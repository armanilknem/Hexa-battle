package com.tdt4240.group3.model

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.model.ecs.components.*
import ktx.ashley.*

object HexMapService {
    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val cityFamily = allOf(PositionComponent::class, CityComponent::class).get()

    fun findTileAt(engine: Engine, worldX: Float, worldY: Float): Entity? {
        val entities = engine.getEntitiesFor(tileFamily)
        if (entities.size() == 0) return null

        return entities.minByOrNull { distSq(it, worldX, worldY) }
            ?.takeIf { distSq(it, worldX, worldY) < 256f }
    }

    fun findTroopAt(engine: Engine, worldX: Float, worldY: Float, exclude: Entity? = null): Entity? {
        return engine.getEntitiesFor(troopFamily).firstOrNull {
            it != exclude && distSq(it, worldX, worldY) < 256f
        }
    }

    fun findCityAt(engine: Engine, worldX: Float, worldY: Float): Entity? {
        return engine.getEntitiesFor(cityFamily).firstOrNull {
            distSq(it, worldX, worldY) < 256f
        }
    }

    private fun distSq(entity: Entity, x: Float, y: Float): Float {
        val pos = entity[PositionComponent.mapper] ?: return Float.MAX_VALUE
        val dx = pos.x - x
        val dy = pos.y - y
        return dx * dx + dy * dy
    }
}
