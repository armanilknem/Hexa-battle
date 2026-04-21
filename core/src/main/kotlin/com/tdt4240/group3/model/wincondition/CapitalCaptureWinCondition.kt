package com.tdt4240.group3.model.wincondition

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * A team is eliminated when it no longer owns any capital.
 * The last surviving team wins.
 */
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
            if (team !in gs.eliminatedTeams && team !in capitalOwners) {
                gs.eliminatedTeams.add(team)
            }
        }
    }

    override fun findWinner(gs: GameStateComponent): Team? =
        gs.activeTeams.filterNot { it in gs.eliminatedTeams }.singleOrNull()
}
