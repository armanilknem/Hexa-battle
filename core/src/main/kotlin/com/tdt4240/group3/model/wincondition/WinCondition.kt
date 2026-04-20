package com.tdt4240.group3.model.wincondition

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.GameStateComponent

interface WinCondition {
    fun isGameInitialized(engine: Engine): Boolean
    fun checkEliminations(engine: Engine, gs: GameStateComponent)
    fun findWinner(gs: GameStateComponent): Team?
}
