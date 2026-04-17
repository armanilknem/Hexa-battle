package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.HexMapService
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import ktx.ashley.*

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
            // Case 1: Deselect already selected troop (must be before move, since troop's own tile is highlighted)
            clickedTroop != null && clickedTroop == selectedTroop -> {
                clearSelectedTroops()
            }

            // Case 2: Move Troop
            clickedTile != null && clickedTile.has(HighlightedComponent.mapper) && selectedTroop != null -> {
                handleMoveIntent(selectedTroop, clickedTile)
            }

            // Case 3: Select Troop
            clickedTroop != null && clickedTroop.has(SelectableComponent.mapper) -> {
                clearSelectedTroops()
                clickedTroop.add(engine.createComponent(SelectedComponent::class.java))
            }

            // Case 4: Deselect (clicked empty area)
            else -> {
                clearSelectedTroops()
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

        // Decrement move counter in the GameState
        engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper)
            ?.let { it.movesLeft-- }

        // Check for collision at target
        val targetOccupied = HexMapService.findTroopAt(engine, tilePos.x.toFloat(), tilePos.y.toFloat()) != null ||
            HexMapService.findCityAt(engine, tilePos.x.toFloat(), tilePos.y.toFloat()) != null

        if (targetOccupied) {
            troop.add(engine.createComponent(CollidingComponent::class.java))
        }

        troop.remove<SelectableComponent>()
        clearSelectedTroops()
    }

    private fun findSelectedTroop() = engine.getEntitiesFor(allOf(SelectedComponent::class, TroopComponent::class).get()).firstOrNull()

    private fun clearSelectedTroops() {
        engine.getEntitiesFor(allOf(SelectedComponent::class).get()).forEach { it.remove<SelectedComponent>() }
    }
}
