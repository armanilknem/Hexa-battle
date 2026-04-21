package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import com.tdt4240.group3.model.hexmap.MapCalculations.hexDistance
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.has
import ktx.ashley.remove

/**
 * Manages [HighlightedComponent] markers each frame:
 * - Highlights troops that belong to [myTeam] and still have moves available.
 * - When a troop is selected, highlights all tiles within its movement range.
 *
 * Note: reachable-tile highlights currently include water tiles regardless of
 * [MovementComponent.canCrossWater]; this will need updating if water traversal is added.
 */
class TroopHighlightSystem(
    private val turnSystem: TurnSystem,
    private val myTeam: Team
) : EntitySystem() {

    private val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val selectedTroopFamily = allOf(
        SelectedComponent::class,
        TroopComponent::class,
        PositionComponent::class,
        MovementComponent::class
    ).get()

    override fun update(deltaTime: Float) {
        updateUnmovedTroopHighlights()
        updateReachableTileHighlights()
    }

    private fun updateUnmovedTroopHighlights() {
        engine.getEntitiesFor(troopFamily).forEach { entity ->
            val team = entity[TeamComponent.mapper] ?: return@forEach
            val isMyTurn = turnSystem.isCurrentTeam(team.team)
            val canMove  = entity.has(SelectableComponent.mapper)
            val isMyTroop = team.team == myTeam

            if (isMyTurn && canMove && isMyTroop) {
                if (!entity.has(HighlightedComponent.mapper)) {
                    entity.add(HighlightedComponent())
                }
            } else {
                entity.remove<HighlightedComponent>()
            }
        }
    }

    private fun updateReachableTileHighlights() {
        val selectedTroop = engine.getEntitiesFor(selectedTroopFamily).firstOrNull()
        val tiles = engine.getEntitiesFor(tileFamily)

        if (selectedTroop != null) {
            val troopPos = selectedTroop[PositionComponent.mapper]!!
            val movement = selectedTroop[MovementComponent.mapper] ?: return
            tiles.forEach { tile ->
                val tilePos = tile[PositionComponent.mapper]!!
                if (hexDistance(troopPos.q, troopPos.r, tilePos.q, tilePos.r) <= movement.moveRange) {
                    if (!tile.has(HighlightedComponent.mapper)) {
                        tile.add(HighlightedComponent())
                    }
                } else {
                    tile.remove<HighlightedComponent>()
                }
            }
        } else {
            tiles.forEach { tile -> tile.remove<HighlightedComponent>() }
        }
    }
}
