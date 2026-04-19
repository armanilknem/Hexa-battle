package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.Team
import ktx.ashley.allOf
import ktx.ashley.get

class WinSystem : EntitySystem() {
    var onWin: ((Team) -> Unit)? = null
    var onPlayerEliminated: ((Team) -> Unit)? = null
    private var winTriggered = false

    private val capitalFamily = allOf(CapitalComponent::class, TeamComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class).get()

    override fun update(deltaTime: Float) {
        if (winTriggered) return

        val gsEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gsEntity[GameStateComponent.mapper] ?: return
        val capitals = engine.getEntitiesFor(capitalFamily)

        if (capitals.size() == 0) return
        // Wait until the game is initialised (at least one capital has a real owner)
        if (capitals.none { it[TeamComponent.mapper]?.team != Team.NONE }) return

        val capitalOwners = capitals.map { it[TeamComponent.mapper]?.team ?: Team.NONE }
        checkEliminations(gs, capitalOwners)

        val survivors = gs.activeTeams.filter { !gs.eliminatedTeams.contains(it) }
        if (survivors.size == 1) {
            winTriggered = true
            onWin?.invoke(survivors.first())
        }
    }

    private fun checkEliminations(gs: GameStateComponent, capitalOwners: List<Team>) {
        gs.activeTeams.forEach { team ->
            if (gs.eliminatedTeams.contains(team)) return@forEach
            if (capitalOwners.none { it == team }) {
                gs.eliminatedTeams.add(team)
                onPlayerEliminated?.invoke(team)
            }
        }
    }

    fun reset() {
        winTriggered = false
    }
}
