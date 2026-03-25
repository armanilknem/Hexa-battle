package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.model.components.HexComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class SelectionSystem : EntitySystem() {

    var selectedTroop: Entity? = null
        private set

    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class, HexComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class, HexComponent::class).get()

    // Called from PlayScreen on touch
    fun handleTouch(worldX: Float, worldY: Float) {
        Gdx.app.log("SelectionSystem", "touch at $worldX, $worldY")
        val clickedTroop = findTroopAt(worldX, worldY)
        val clickedTile  = findTileAt(worldX, worldY)

        when {
            // Tap a troop — select it and highlight reachable tiles
            clickedTroop != null -> {
                clearHighlights()
                selectedTroop = clickedTroop
                highlightReachableTiles(clickedTroop)
            }
            // Tap a highlighted tile — move the selected troop there
            clickedTile != null && clickedTile[TileComponent.mapper]?.isHighlighted == true -> {
                selectedTroop?.let { moveTroop(it, clickedTile) }
                clearHighlights()
                selectedTroop = null
            }
            // Tap empty space — deselect
            else -> {
                clearHighlights()
                selectedTroop = null
            }
        }
    }

    private fun highlightReachableTiles(troop: Entity) {
        val troopHex = troop[HexComponent.mapper] ?: return
        val range = 2

        engine.entities.forEach { entity ->
            if (tileFamily.matches(entity)) {
                val hex = entity[HexComponent.mapper] ?: return@forEach
                if (hexDistance(troopHex.q, troopHex.r, hex.q, hex.r) <= range) {
                    entity[TileComponent.mapper]?.isHighlighted = true
                }
            }
        }
    }

    private fun moveTroop(troop: Entity, targetTile: Entity) {
        val targetPos = targetTile[PositionComponent.mapper] ?: return
        val targetHex = targetTile[HexComponent.mapper] ?: return
        val troopPos  = troop[PositionComponent.mapper] ?: return
        val troopHex  = troop[HexComponent.mapper] ?: return

        troopPos.x = targetPos.x
        troopPos.y = targetPos.y
        troopHex.q = targetHex.q
        troopHex.r = targetHex.r
    }

    private fun clearHighlights() {
        engine.entities.forEach { entity ->
            entity[TileComponent.mapper]?.isHighlighted = false
        }
    }

    private fun findTroopAt(worldX: Float, worldY: Float): Entity? {
        // Log ALL entities to see what's actually in the engine
        engine.entities.forEach { entity ->
            val pos = entity[PositionComponent.mapper]
            Gdx.app.log("SelectionSystem", "entity: pos=${pos?.x},${pos?.y} " +
                "troopMatch=${troopFamily.matches(entity)}")
        }

        return engine.entities.firstOrNull { entity ->
            if (!troopFamily.matches(entity)) return@firstOrNull false
            val pos = entity[PositionComponent.mapper] ?: return@firstOrNull false
            val dx = Math.abs(pos.x.toFloat() - worldX)
            val dy = Math.abs(pos.y.toFloat() - worldY)
            Gdx.app.log("SelectionSystem", "troop at ${pos.x},${pos.y} | touch at $worldX,$worldY | dx=$dx dy=$dy")
            dx < 32f && dy < 32f
        }
    }


    private fun findTileAt(worldX: Float, worldY: Float): Entity? {
        return engine.entities.minByOrNull { entity ->
            if (!tileFamily.matches(entity)) return@minByOrNull Float.MAX_VALUE
            val pos = entity[PositionComponent.mapper] ?: return@minByOrNull Float.MAX_VALUE
            val dx = pos.x - worldX
            val dy = pos.y - worldY
            dx * dx + dy * dy
        }?.takeIf {
            val pos = it[PositionComponent.mapper] ?: return@takeIf false
            Math.abs(pos.x - worldX) < 32f && Math.abs(pos.y - worldY) < 32f
        }
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (Math.abs(q1 - q2) +
            Math.abs(q1 + r1 - q2 - r2) +
            Math.abs(r1 - r2)) / 2
    }
}
