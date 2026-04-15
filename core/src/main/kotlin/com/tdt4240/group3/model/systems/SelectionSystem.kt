package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.HighlightedComponent
import com.tdt4240.group3.model.components.marker.SelectedComponent
import ktx.ashley.allOf
import ktx.ashley.get

class SelectionSystem() : EntitySystem() {

    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()

    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()

    private val troopFamily =
        allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()

    fun handleTouch(worldX: Float, worldY: Float) {
        val clickedTroop = findTroopAt(worldX, worldY)
        val clickedTile = findTileAt(worldX, worldY)
        val selectedTroop = findSelectedTroop()

        when {
            // moving a troop to a tile
            clickedTile != null && clickedTile.getComponent(HighlightedComponent::class.java)
                != null && selectedTroop != null -> {
                val intent = engine.createComponent(MoveIntentComponent::class.java)
                val tilePos = clickedTile.getComponent(PositionComponent::class.java) ?: return

                intent.targetQ = tilePos.q
                intent.targetR = tilePos.r
                selectedTroop.add(intent)

                val targetTroop = engine.getEntitiesFor(troopFamily).firstOrNull { troop ->
                    if (troop == selectedTroop) return@firstOrNull false
                    val pos = troop[PositionComponent.mapper] ?: return@firstOrNull false
                    pos.q == tilePos.q && pos.r == tilePos.r
                }

                val targetCity = engine.entities.firstOrNull { entity ->
                    entity.getComponent(CityComponent::class.java) != null &&
                        entity[PositionComponent.mapper]?.q == tilePos.q &&
                        entity[PositionComponent.mapper]?.r == tilePos.r
                }

                if (targetTroop != null || targetCity != null) {
                    selectedTroop.add(engine.createComponent(CollidingComponent::class.java))
                }

                if (targetTroop != null) {
                    targetTroop.remove(SelectableComponent::class.java)
                }

                selectedTroop.remove(SelectableComponent::class.java)

                clearSelectedTroops()
                clearHighlights()
            }

            // show highlighted area when selecting troop
            clickedTroop != null && clickedTroop.getComponent(SelectableComponent::class.java) != null -> {
                clearSelectedTroops()
                clearHighlights()
                clickedTroop.add(engine.createComponent(SelectedComponent::class.java))
                highlightReachableTiles(clickedTroop)
            }

            // unselect when clicking outside or on same troop twice //FIX:
            else -> {
                clearSelectedTroops()
                clearHighlights()
            }
        }
    }

    private fun findSelectedTroop(): Entity? {
        return engine.getEntitiesFor(allOf(SelectedComponent::class, TroopComponent::class).get()).firstOrNull()
    }

    private fun clearSelectedTroops() {
        val selected = engine.getEntitiesFor(allOf(SelectedComponent::class).get())
        selected.toList().forEach { it.remove(SelectedComponent::class.java) }
    }

    private fun highlightReachableTiles(troop: Entity) {
        val troopPos = troop[PositionComponent.mapper] ?: return
        engine.entities.forEach { entity ->
            if (!tileFamily.matches(entity)) return@forEach
            val hex = entity[PositionComponent.mapper] ?: return@forEach
            if (hexDistance(troopPos.q, troopPos.r, hex.q, hex.r) <= 2) {
                entity.add(engine.createComponent(HighlightedComponent::class.java))
            }
        }
    }

    private fun clearHighlights() {
        val highlighted = engine.getEntitiesFor(allOf(HighlightedComponent::class).get())
        highlighted.toList().forEach { it.remove(HighlightedComponent::class.java) }
    }

    private fun findTroopAt(worldX: Float, worldY: Float): Entity? {
        return engine.entities.firstOrNull { entity ->
            if (!troopFamily.matches(entity)) return@firstOrNull false
            val pos = entity[PositionComponent.mapper] ?: return@firstOrNull false
            Math.abs(pos.x.toFloat() - worldX) < 16f && Math.abs(pos.y.toFloat() - worldY) < 16f
        }
    }

    private fun findTileAt(worldX: Float, worldY: Float): Entity? {
        return engine.entities
            .filter { tileFamily.matches(it) }
            .minByOrNull { entity ->
                val pos = entity[PositionComponent.mapper] ?: return@minByOrNull Float.MAX_VALUE
                val dx = pos.x.toFloat() - worldX
                val dy = pos.y.toFloat() - worldY
                dx * dx + dy * dy
            }
            ?.takeIf { entity ->
                val pos = entity[PositionComponent.mapper] ?: return@takeIf false
                Math.abs(pos.x.toFloat() - worldX) < 16f && Math.abs(pos.y.toFloat() - worldY) < 16f
            }
    }

    fun findCityAt(worldX: Float, worldY: Float): Entity? {
        return engine.entities.firstOrNull { entity ->
            if (!cityFamily.matches(entity)) return@firstOrNull false
            val pos = entity[PositionComponent.mapper] ?: return@firstOrNull false
            Math.abs(pos.x - worldX) < 16f && Math.abs(pos.y - worldY) < 16f
        }
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (Math.abs(q1 - q2) +
            Math.abs(q1 + r1 - q2 - r2) +
            Math.abs(r1 - r2)) / 2
    }
}
