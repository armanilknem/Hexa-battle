package com.tdt4240.group3.model.wincondition

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.GameStateComponent

/**
 * Strategy interface for win/elimination logic.
 * Implementations define when the game is considered initialised, which teams are
 * eliminated, and when a single winner can be declared.
 */
interface WinCondition {

    /** Returns true once the game world is in a state where win checking is meaningful
     *  (e.g. capitals have been placed and at least one is owned). */
    fun isGameInitialized(engine: Engine): Boolean

    /** Inspects the current game state and adds any newly eliminated teams to
     *  [GameStateComponent.eliminatedTeams]. Idempotent — safe to call every frame. */
    fun checkEliminations(engine: Engine, gs: GameStateComponent)

    /** Returns the winning [Team] if exactly one active team remains, or null otherwise. */
    fun findWinner(gs: GameStateComponent): Team?
}
