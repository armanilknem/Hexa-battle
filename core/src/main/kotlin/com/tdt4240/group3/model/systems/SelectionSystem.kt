package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.tdt4240.group3.controller.TurnController
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent
import ktx.ashley.allOf
import ktx.ashley.get

class SelectionSystem(private val turnSystem: TurnSystem) : EntitySystem() {

    var onTurnEnd: (() -> Unit)? = null  // PlayScreen sets this
    var selectedTroop: Entity? = null
        private set

    private val tileFamily = allOf(PositionComponent::class, TileComponent::class).get()

    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()

    private val troopFamily =
        allOf(PositionComponent::class, TroopComponent::class, TeamComponent::class).get()

    fun handleTouch(worldX: Float, worldY: Float) {
        val clickedTroop = findTroopAt(worldX, worldY)
        val clickedTile = findTileAt(worldX, worldY)

        if (selectedTroop != null && clickedTile != null && clickedTile[TileComponent.mapper]?.isHighlighted == true) {
            selectedTroop?.get(TroopComponent.mapper)?.isClicked = false
            moveTroop(selectedTroop!!, clickedTile)
            clearHighlights()
            selectedTroop = null
            return
        }

        when {
            clickedTroop != null -> {
                val team = clickedTroop[TeamComponent.mapper]?.team ?: return
                if (!turnSystem.isCurrentTeam(team)) return
                selectedTroop?.get(TroopComponent.mapper)?.isClicked = false
                if (clickedTroop[TroopComponent.mapper]?.isMoved == true) return
                clearHighlights()
                selectedTroop = clickedTroop
                clickedTroop[TroopComponent.mapper]?.hasBeenClicked()
                highlightReachableTiles(clickedTroop)
            }

            else -> {
                selectedTroop?.get(TroopComponent.mapper)?.isClicked = false
                clearHighlights()
                selectedTroop = null
            }
        }
    }

    private fun highlightReachableTiles(troop: Entity) {
        val troopPos = troop[PositionComponent.mapper] ?: return
        engine.entities.forEach { entity ->
            if (!tileFamily.matches(entity)) return@forEach
            val hex = entity[PositionComponent.mapper] ?: return@forEach
            if (hexDistance(troopPos.q, troopPos.r, hex.q, hex.r) <= 2) {
                entity[TileComponent.mapper]?.isHighlighted = true
            }
        }
    }

    private fun moveTroop(troop: Entity, targetTile: Entity) {
        val targetPos = targetTile[PositionComponent.mapper] ?: return
        val troopPos = troop[PositionComponent.mapper] ?: return

        troopPos.prevQ = troopPos.q
        troopPos.prevR = troopPos.r

        troopPos.q = targetPos.q
        troopPos.r = targetPos.r

        val troopComp = troop[TroopComponent.mapper]
        troopComp?.hasBeenMoved()
        troopComp?.isClicked = false
        troopComp?.colliding()

        if (allTroopsMoved()) {
            onTurnEnd?.invoke()
        }
    }

    private fun allTroopsMoved(): Boolean {
        val troopFamily = allOf(TroopComponent::class, TeamComponent::class).get()
        return engine.getEntitiesFor(troopFamily)
            .filter { TeamComponent.mapper.get(it)?.team == turnSystem.currentTeam }
            .all { TroopComponent.mapper.get(it)?.isMoved == true }
    }

    private fun clearHighlights() {
        engine.entities.forEach { entity ->
            entity[TileComponent.mapper]?.isHighlighted = false
        }
    }

    private fun findTroopAt(worldX: Float, worldY: Float): Entity? {
        return engine.entities.firstOrNull { entity ->
            if (!troopFamily.matches(entity)) return@firstOrNull false
            val pos = entity[PositionComponent.mapper] ?: return@firstOrNull false
            Math.abs(pos.x.toFloat() - worldX) < 16f && Math.abs(pos.y.toFloat() - worldY) < 16f
        }
    }

    fun findTileAt(worldX: Float, worldY: Float): Entity? {
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
