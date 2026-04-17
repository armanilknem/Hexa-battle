package com.tdt4240.group3.model.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.HexMapService
import com.tdt4240.group3.model.ecs.components.TouchInputComponent
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.has
import ktx.ashley.remove

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
        val currentPos = troop[PositionComponent.mapper] ?: return

        // Reselect troop to cancel move
        if (currentPos.q == tilePos.q && currentPos.r == tilePos.r) {
            clearSelectedTroops()
            clearHighlights()
            return // Move is not counted
        }

        // Find if there is a troop at the destination
        val targetTroopEntity = engine.getEntitiesFor(
            allOf(TroopComponent::class, PositionComponent::class, CombatComponent::class).get()
        )
            .find {
                val p = it[PositionComponent.mapper]
                p?.q == tilePos.q && p?.r == tilePos.r
            }

        // Check for overflow before moving
        if (targetTroopEntity != null) {
            val targetTroopData = targetTroopEntity[TroopComponent.mapper]!!
            val targetCombat = targetTroopEntity[CombatComponent.mapper] ?: return
            val movingCombat = troop[CombatComponent.mapper] ?: return
            val targetTeam = targetTroopEntity[TeamComponent.mapper]?.team
            val movingTeam = troop[TeamComponent.mapper]?.team

            // Cancel the move if a friendly merge is not allowed or the target stack is already full.
            if (targetTeam == movingTeam &&
                (!movingCombat.canMergeFriendly ||
                    !targetCombat.canMergeFriendly ||
                    targetTroopData.strength >= targetCombat.maxStackSize)
            ) {
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

    private fun highlightReachableTiles(troop: Entity) {
        val troopPos = troop[PositionComponent.mapper] ?: return
        val movement = troop[MovementComponent.mapper] ?: return
        val tiles = engine.getEntitiesFor(allOf(PositionComponent::class, TileComponent::class).get())

        tiles.forEach { tile ->
            val hex = tile[PositionComponent.mapper]!!
            if (hexDistance(troopPos.q, troopPos.r, hex.q, hex.r) <= movement.moveRange) {
                tile.add(engine.createComponent(HighlightedComponent::class.java))
            }
        }
    }

    private fun findSelectedTroop() = engine.getEntitiesFor(
        allOf(SelectedComponent::class, TroopComponent::class, MovementComponent::class, CombatComponent::class).get()
    ).firstOrNull()

    private fun clearSelectedTroops() {
        engine.getEntitiesFor(allOf(SelectedComponent::class).get()).forEach { it.remove<SelectedComponent>() }
    }
}
