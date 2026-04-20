package com.tdt4240.group3.model.wincondition

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.allOf
import ktx.ashley.get

class CapitalCaptureWinCondition : WinCondition {
    private val capitalFamily = allOf(CapitalComponent::class, TeamComponent::class).get()

    override fun isGameInitialized(engine: Engine): Boolean {
        val capitals = engine.getEntitiesFor(capitalFamily)
        return capitals.size() > 0 && capitals.any { it[TeamComponent.mapper]?.team != Team.NONE }
    }

    override fun checkEliminations(engine: Engine, gs: GameStateComponent) {
        val capitalOwners = engine.getEntitiesFor(capitalFamily)
            .map { it[TeamComponent.mapper]?.team ?: Team.NONE }
        gs.activeTeams.forEach { team ->
            if (!gs.eliminatedTeams.contains(team) && capitalOwners.none { it == team }) {
                gs.eliminatedTeams.add(team)
            }
        }
    }

    override fun findWinner(gs: GameStateComponent): Team? {
        val survivors = gs.activeTeams.filter { !gs.eliminatedTeams.contains(it) }
        return if (survivors.size == 1) survivors.first() else null
    }
}
