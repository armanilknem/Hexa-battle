package com.tdt4240.group3.controller

import com.tdt4240.group3.controller.systems.TurnSystem
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.view.states.PauseState
import com.tdt4240.group3.view.states.PlayerTurnState
import com.tdt4240.group3.view.states.EnemyTurnState
import com.tdt4240.group3.view.states.PlaySubState
import ktx.ashley.get

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
