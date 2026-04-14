package com.tdt4240.group3.controller

import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.view.states.PlaySubState

class PauseController(
    private val turnSystem: TurnSystem,
    private val playScreen: PlayScreen

) {
    fun togglePause(currentState: PlaySubState) {
       /* if (currentState is PauseState) {
            when (turnSystem.currentTeam) {
                TeamComponent.TeamName.BLUE -> playScreen.changeState(PlayerTurnState())
                TeamComponent.TeamName.RED  -> playScreen.changeState(EnemyTurnState())
                TeamComponent.TeamName.NONE -> throw IllegalStateException("currentTeam should never be NONE")
            }
        } else {
            playScreen.changeState(PauseState())
        }*/
    }
}
