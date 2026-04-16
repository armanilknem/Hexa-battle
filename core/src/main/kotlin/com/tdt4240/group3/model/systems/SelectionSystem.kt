package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.HexMapService
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import ktx.ashley.*

class SelectionSystem : IteratingSystem(allOf(TouchInputComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val input = entity[TouchInputComponent.mapper] ?: return

        val clickedTroop = HexMapService.findTroopAt(engine, input.x, input.y)
        val clickedTile = HexMapService.findTileAt(engine, input.x, input.y)
        val selectedTroop = findSelectedTroop()

        processSelectionLogic(clickedTroop, clickedTile, selectedTroop)

        engine.removeEntity(entity)
    }

    private fun processSelectionLogic(clickedTroop: Entity?, clickedTile: Entity?, selectedTroop: Entity?) {
        when {
            // Case 1: Move Troop
            clickedTile != null && clickedTile.has(HighlightedComponent.mapper) && selectedTroop != null -> {
                handleMoveIntent(selectedTroop, clickedTile)
            }

            // Case 2: Select Troop
            clickedTroop != null && clickedTroop.has(SelectableComponent.mapper) -> {
                clearSelectedTroops()
                clearHighlights()
                clickedTroop.add(engine.createComponent(SelectedComponent::class.java))
                highlightReachableTiles(clickedTroop)
            }

            // Case 3: Deselect
            else -> {
                clearSelectedTroops()
                clearHighlights()
            }
        }
    }

    private fun handleMoveIntent(troop: Entity, tile: Entity) {
        val tilePos = tile[PositionComponent.mapper] ?: return

        // Add Move Intent
        troop.add(engine.createComponent(MoveIntentComponent::class.java).apply {
            targetQ = tilePos.q
            targetR = tilePos.r
        })

        // Check for collision at target
        val targetOccupied = HexMapService.findTroopAt(engine, tilePos.x.toFloat(), tilePos.y.toFloat()) != null ||
            HexMapService.findCityAt(engine, tilePos.x.toFloat(), tilePos.y.toFloat()) != null

        if (targetOccupied) {
            troop.add(engine.createComponent(CollidingComponent::class.java))
        }

        troop.remove<SelectableComponent>()
        clearSelectedTroops()
        clearHighlights()
    }

    // Helper methods (highlightReachableTiles, etc) stay here but use
    // engine.getEntitiesFor(family) instead of engine.entities.filter
    private fun highlightReachableTiles(troop: Entity) {
        val troopPos = troop[PositionComponent.mapper] ?: return
        val tiles = engine.getEntitiesFor(allOf(PositionComponent::class, TileComponent::class).get())

        tiles.forEach { tile ->
            val hex = tile[PositionComponent.mapper]!!
            if (hexDistance(troopPos.q, troopPos.r, hex.q, hex.r) <= 2) {
                tile.add(engine.createComponent(HighlightedComponent::class.java))
            }
        }
    }

    private fun findSelectedTroop() = engine.getEntitiesFor(allOf(SelectedComponent::class, TroopComponent::class).get()).firstOrNull()

    private fun clearSelectedTroops() {
        engine.getEntitiesFor(allOf(SelectedComponent::class).get()).forEach { it.remove<SelectedComponent>() }
    }

    private fun clearHighlights() {
        engine.getEntitiesFor(allOf(HighlightedComponent::class).get()).forEach { it.remove<HighlightedComponent>() }
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (Math.abs(q1 - q2) + Math.abs(q1 + r1 - q2 - r2) + Math.abs(r1 - r2)) / 2
    }
}
