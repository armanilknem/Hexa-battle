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

/**
 * Processes one-shot [TouchInputComponent] command entities each frame.
 * Handles troop selection, deselection, and move-intent creation based on what was tapped.
 * The command entity is removed after processing.
 */
class SelectionSystem : IteratingSystem(allOf(TouchInputComponent::class).get()) {

    private val gameStateFamily    = allOf(GameStateComponent::class).get()
    private val troopAtTileFamily  = allOf(TroopComponent::class, PositionComponent::class).get()
    private val selectedFamily     = allOf(SelectedComponent::class).get()
    private val selectedTroopFamily = allOf(
        SelectedComponent::class,
        TroopComponent::class,
        MovementComponent::class,
        CombatComponent::class
    ).get()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val input = entity[TouchInputComponent.mapper] ?: return

        val clickedTroop  = HexMapQueries.findTroopAt(engine, input.x, input.y)
        val clickedTile   = HexMapQueries.findTileAt(engine, input.x, input.y)
        val selectedTroop = findSelectedTroop()

        processSelectionLogic(clickedTroop, clickedTile, selectedTroop)
        engine.removeEntity(entity)
    }

    private fun processSelectionLogic(clickedTroop: Entity?, clickedTile: Entity?, selectedTroop: Entity?) {
        when {
            clickedTroop != null && clickedTroop == selectedTroop ->
                clearSelectedTroops()

            clickedTile != null && clickedTile.has(HighlightedComponent.mapper) && selectedTroop != null ->
                handleMoveIntent(selectedTroop, clickedTile)

            clickedTroop != null && clickedTroop.has(SelectableComponent.mapper) -> {
                clearSelectedTroops()
                clickedTroop.add(SelectedComponent())
            }

            else -> clearSelectedTroops()
        }
    }

    private fun handleMoveIntent(troop: Entity, tile: Entity) {
        val tilePos    = tile[PositionComponent.mapper]  ?: return
        val currentPos = troop[PositionComponent.mapper] ?: return

        if (currentPos.q == tilePos.q && currentPos.r == tilePos.r) {
            clearSelectedTroops()
            return
        }

        val targetTroopEntity = engine.getEntitiesFor(troopAtTileFamily).find {
            val p = it[PositionComponent.mapper]
            p?.q == tilePos.q && p.r == tilePos.r
        }

        if (targetTroopEntity != null) {
            val targetTroopData = targetTroopEntity[TroopComponent.mapper]!!
            val targetCombat    = targetTroopEntity[CombatComponent.mapper]
            val targetTeam      = targetTroopEntity[TeamComponent.mapper]?.team
            val movingTeam      = troop[TeamComponent.mapper]?.team

            if (targetTeam == movingTeam && targetCombat != null && targetTroopData.strength >= targetCombat.maxStackSize) {
                clearSelectedTroops()
                return
            }
        }

        troop.add(MoveIntentComponent().apply {
            targetQ = tilePos.q
            targetR = tilePos.r
        })

        engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper)
            ?.let { it.movesLeft-- }

        if (targetTroopEntity != null) {
            troop.add(CollidingComponent())
        }

        troop.remove<SelectableComponent>()
        clearSelectedTroops()
    }

    private fun findSelectedTroop() =
        engine.getEntitiesFor(selectedTroopFamily).firstOrNull()

    private fun clearSelectedTroops() =
        engine.getEntitiesFor(selectedFamily).forEach { it.remove<SelectedComponent>() }
}
