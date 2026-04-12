package com.tdt4240.group3.controller

import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.states.playstate.EnemyTurnState
import com.tdt4240.group3.states.playstate.PlayerTurnState

class TurnController(
    private val turnSystem: TurnSystem,   // reads/advances the model
    private val playScreen: PlayScreen    // tells the view to react
) {
    fun endTurn() {
        turnSystem.endTurn()  // 1. model goes first

        playScreen.updateLabel()  // 2. view updates label

        when (turnSystem.currentTeam) {  // 3. view switches state
            TeamComponent.TeamName.BLUE -> playScreen.changeState(PlayerTurnState())
            TeamComponent.TeamName.RED  -> playScreen.changeState(EnemyTurnState())
            else -> throw IllegalStateException("currentTeam should never be NONE")
        }
    }
}
