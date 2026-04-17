package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.components.marker.HighlightedComponent
import com.tdt4240.group3.model.components.marker.SelectableComponent
import com.tdt4240.group3.model.components.marker.SelectedComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.has
import ktx.ashley.remove

class TroopHighlightSystem(private val turnSystem: TurnSystem) : EntitySystem() {

    private val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()
    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()
    private val selectedTroopFamily = allOf(SelectedComponent::class, TroopComponent::class, PositionComponent::class).get()

    override fun update(deltaTime: Float) {
        updateUnmovedTroopHighlights()
        updateReachableTileHighlights()
    }

    private fun updateUnmovedTroopHighlights() {
        engine.getEntitiesFor(troopFamily).forEach { entity ->
            val team = entity[TeamComponent.mapper] ?: return@forEach
            val isMyTurn = turnSystem.isCurrentTeam(team.team)
            val canMove = entity.has(SelectableComponent.mapper)

            if (isMyTurn && canMove) {
                if (!entity.has(HighlightedComponent.mapper)) {
                    entity.add(engine.createComponent(HighlightedComponent::class.java))
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
            tiles.forEach { tile ->
                val tilePos = tile[PositionComponent.mapper]!!
                if (hexDistance(troopPos.q, troopPos.r, tilePos.q, tilePos.r) <= 2) {
                    if (!tile.has(HighlightedComponent.mapper)) {
                        tile.add(engine.createComponent(HighlightedComponent::class.java))
                    }
                } else {
                    tile.remove<HighlightedComponent>()
                }
            }
        } else {
            tiles.forEach { tile -> tile.remove<HighlightedComponent>() }
        }
    }

    private fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (Math.abs(q1 - q2) + Math.abs(q1 + r1 - q2 - r2) + Math.abs(r1 - r2)) / 2
    }
}
