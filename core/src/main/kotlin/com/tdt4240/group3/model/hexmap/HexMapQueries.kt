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

/** Spatial queries against the hex map. All functions use Ashley component families so
 *  only the relevant entity subset is iterated — never the full [Engine.entities] list. */
object HexMapQueries {

    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()

    /** Returns the tile whose centre is closest to ([worldX], [worldY]),
     *  or null if no tile centre is within [GameConstants.HEX_PICK_RADIUS_SQ]. */
    fun findTileAt(engine: Engine, worldX: Float, worldY: Float): Entity? =
        engine.getEntitiesFor(tileFamily)
            .minByOrNull { distSq(it, worldX, worldY) }
            ?.takeIf { distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ }

    /** Returns the first troop whose centre is within [GameConstants.HEX_PICK_RADIUS_SQ]
     *  of ([worldX], [worldY]), or null if none. */
    fun findTroopAt(engine: Engine, worldX: Float, worldY: Float): Entity? =
        engine.getEntitiesFor(troopFamily).firstOrNull {
            distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ
        }

    /** Returns the city whose centre is within [GameConstants.HEX_PICK_RADIUS_SQ]
     *  of ([worldX], [worldY]), or null if none. */
    fun findCityAtByXY(engine: Engine, worldX: Float, worldY: Float): Entity? =
        engine.getEntitiesFor(cityFamily).firstOrNull {
            distSq(it, worldX, worldY) < GameConstants.HEX_PICK_RADIUS_SQ
        }

    /** Returns the city at axial coordinates ([q], [r]), or null if none. */
    fun findCityAtByQR(engine: Engine, q: Int, r: Int): Entity? =
        engine.getEntitiesFor(cityFamily).firstOrNull { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }

    /** Returns true if any troop occupies axial coordinates ([q], [r]). */
    fun hasTroopAt(engine: Engine, q: Int, r: Int): Boolean =
        engine.getEntitiesFor(troopFamily).any { entity ->
            val pos = entity[PositionComponent.mapper]
            pos?.q == q && pos.r == r
        }

    /** Returns true if any city occupies axial coordinates ([q], [r]). */
    fun hasCityAt(engine: Engine, q: Int, r: Int): Boolean =
        findCityAtByQR(engine, q, r) != null

    private fun distSq(entity: Entity, x: Float, y: Float): Float {
        val pos = entity[PositionComponent.mapper] ?: return Float.MAX_VALUE
        val dx = pos.x - x
        val dy = pos.y - y
        return dx * dx + dy * dy
    }
}
