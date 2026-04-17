package com.tdt4240.group3.model.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get

class SelectionSystem() : EntitySystem() {

class SelectionSystem : IteratingSystem(allOf(TouchInputComponent::class).get()) {

    private val gameStateFamily = allOf(GameStateComponent::class).get()

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
        val troopData = troop[TroopComponent.mapper] ?: return
        val currentPos = troop[PositionComponent.mapper] ?: return

        // Reselect troop to cancel move
        if (currentPos.q == tilePos.q && currentPos.r == tilePos.r) {
            clearSelectedTroops()
            clearHighlights()
            return // Move is not counted
        }

        // Find if there is a troop at the destination
        val targetTroopEntity = engine.getEntitiesFor(allOf(TroopComponent::class, PositionComponent::class).get())
            .find {
                val p = it[PositionComponent.mapper]
                p?.q == tilePos.q && p?.r == tilePos.r
            }

        // Check for overflow before moving
        if (targetTroopEntity != null) {
            val targetTroopData = targetTroopEntity[TroopComponent.mapper]!!
            val targetTeam = targetTroopEntity[TeamComponent.mapper]?.team
            val movingTeam = troop[TeamComponent.mapper]?.team

            // If it's a friendly merge and the target is already at 99
            if (targetTeam == movingTeam && targetTroopData.strength >= 99) {
                println("Move rejected: Target tile already full")
                clearSelectedTroops()
                clearHighlights()
                return // Move is not counted
            }
        }

        // Add Move Intent
        troop.add(engine.createComponent(MoveIntentComponent::class.java).apply {
            targetQ = tilePos.q
            targetR = tilePos.r
        })

        // Decrement move counter in the GameState
        engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper)
            ?.let { it.movesLeft-- }

        // Add CollidingComponent if there is a target troop
        if (targetTroopEntity != null) {
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
