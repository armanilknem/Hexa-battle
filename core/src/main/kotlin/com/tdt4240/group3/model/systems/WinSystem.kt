package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.wincondition.CapitalCaptureWinCondition
import com.tdt4240.group3.model.wincondition.WinCondition
import ktx.ashley.allOf
import ktx.ashley.get

class WinSystem(
    private val winCondition: WinCondition = CapitalCaptureWinCondition()
) : EntitySystem() {
    var onWin: ((Team) -> Unit)? = null
    var onPlayerEliminated: ((Team) -> Unit)? = null
    private var winTriggered = false
    private val notifiedEliminations = mutableSetOf<Team>()

    private val gameStateFamily = allOf(GameStateComponent::class).get()

    override fun update(deltaTime: Float) {
        if (winTriggered) return

        val gsEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gsEntity[GameStateComponent.mapper] ?: return

        if (!winCondition.isGameInitialized(engine)) return

        winCondition.checkEliminations(engine, gs)

        gs.eliminatedTeams.forEach { team ->
            if (notifiedEliminations.add(team)) {
                onPlayerEliminated?.invoke(team)
            }
        }

        winCondition.findWinner(gs)?.let { winner ->
            winTriggered = true
            onWin?.invoke(winner)
        }
    }

    fun reset() {
        winTriggered = false
        notifiedEliminations.clear()
    }
}
