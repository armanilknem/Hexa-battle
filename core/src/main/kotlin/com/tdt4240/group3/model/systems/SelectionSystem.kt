package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.tdt4240.group3.model.hexmap.HexMapQueries
import com.tdt4240.group3.model.components.TouchInputComponent
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.has
import ktx.ashley.remove

class SelectionSystem : IteratingSystem(allOf(TouchInputComponent::class).get()) {

    private val gameStateFamily = allOf(GameStateComponent::class).get()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val input = entity[TouchInputComponent.mapper] ?: return

        val clickedTroop = HexMapQueries.findTroopAt(engine, input.x, input.y)
        val clickedTile = HexMapQueries.findTileAt(engine, input.x, input.y)
        val selectedTroop = findSelectedTroop()

        processSelectionLogic(clickedTroop, clickedTile, selectedTroop)

        engine.removeEntity(entity)
    }

    private fun processSelectionLogic(clickedTroop: Entity?, clickedTile: Entity?, selectedTroop: Entity?) {
        when {
            clickedTroop != null && clickedTroop == selectedTroop -> {
                clearSelectedTroops()
            }

            clickedTile != null && clickedTile.has(HighlightedComponent.mapper) && selectedTroop != null -> {
                handleMoveIntent(selectedTroop, clickedTile)
            }

            clickedTroop != null && clickedTroop.has(SelectableComponent.mapper) -> {
                clearSelectedTroops()
                clickedTroop.add(engine.createComponent(SelectedComponent::class.java))
            }

            else -> {
                clearSelectedTroops()
            }
        }
    }

    private fun handleMoveIntent(troop: Entity, tile: Entity) {
        val tilePos = tile[PositionComponent.mapper] ?: return
        val currentPos = troop[PositionComponent.mapper] ?: return

        if (currentPos.q == tilePos.q && currentPos.r == tilePos.r) {
            clearSelectedTroops()
            return
        }

        val targetTroopEntity = engine.getEntitiesFor(allOf(TroopComponent::class, PositionComponent::class).get())
            .find {
                val p = it[PositionComponent.mapper]
                p?.q == tilePos.q && p?.r == tilePos.r
            }

        if (targetTroopEntity != null) {
            val targetTroopData = targetTroopEntity[TroopComponent.mapper]!!
            val targetCombat = targetTroopEntity[CombatComponent.mapper]
            val targetTeam = targetTroopEntity[TeamComponent.mapper]?.team
            val movingTeam = troop[TeamComponent.mapper]?.team

            if (targetTeam == movingTeam && targetCombat != null && targetTroopData.strength >= targetCombat.maxStackSize) {
                clearSelectedTroops()
                return
            }
        }

        troop.add(engine.createComponent(MoveIntentComponent::class.java).apply {
            targetQ = tilePos.q
            targetR = tilePos.r
        })

        engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper)
            ?.let { it.movesLeft-- }

        if (targetTroopEntity != null) {
            troop.add(engine.createComponent(CollidingComponent::class.java))
        }

        troop.remove<SelectableComponent>()
        clearSelectedTroops()
    }

    private fun findSelectedTroop() = engine.getEntitiesFor(
        allOf(SelectedComponent::class, TroopComponent::class, MovementComponent::class, CombatComponent::class).get()
    ).firstOrNull()

    private fun clearSelectedTroops() {
        engine.getEntitiesFor(allOf(SelectedComponent::class).get()).forEach { it.remove<SelectedComponent>() }
    }
}
