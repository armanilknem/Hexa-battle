package com.tdt4240.group3.controller

import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.states.playstate.EnemyTurnState
import com.tdt4240.group3.states.playstate.PlayerTurnState
import ktx.ashley.allOf

class TurnController(
    private val turnSystem: TurnSystem,
    private val playScreen: PlayScreen,
    private val troopCreationController: TroopCreationController
) {
    fun endTurn() {
        troopCreationController.createTroopsForCurrentTeam()  // spawn for this team
        turnSystem.endTurn()        // model advances
        playScreen.updateLabel()    // view updates

        when (turnSystem.currentTeam) {
            TeamComponent.TeamName.BLUE -> playScreen.changeState(PlayerTurnState())
            TeamComponent.TeamName.RED  -> playScreen.changeState(EnemyTurnState())
            TeamComponent.TeamName.NONE -> throw IllegalStateException("currentTeam should never be NONE")
        }
    }


}
