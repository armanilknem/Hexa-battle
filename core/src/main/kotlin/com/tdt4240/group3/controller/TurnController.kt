package com.tdt4240.group3.controller

import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.controller.systems.TurnSystem
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.view.states.EnemyTurnState
import com.tdt4240.group3.view.states.PlayerTurnState

class TurnController(
    private val turnSystem: TurnSystem,
    private val playScreen: PlayScreen,
    private val troopCreationController: TroopCreationController
) {
    fun endTurn() {
        turnSystem.endTurn()        // model advances
        troopCreationController.createTroopsForCurrentTeam()  // spawn for new team
        playScreen.updateLabel()    // view updates

        when (turnSystem.currentTeam) {
            TeamComponent.TeamName.BLUE -> playScreen.changeState(PlayerTurnState())
            TeamComponent.TeamName.RED  -> playScreen.changeState(EnemyTurnState())
            TeamComponent.TeamName.NONE -> throw IllegalStateException("currentTeam should never be NONE")
        }
    }


}
