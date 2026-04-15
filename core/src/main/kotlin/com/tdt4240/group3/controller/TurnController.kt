package com.tdt4240.group3.controller

import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.view.states.PlayerTurnState
import com.tdt4240.group3.view.states.EnemyTurnState

class TurnController(
    private val turnSystem: TurnSystem,
) {
    fun endTurn() {
        turnSystem.endTurn()
    }
}
