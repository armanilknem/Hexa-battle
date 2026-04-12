package com.tdt4240.group3.controller

import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TurnSystem

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
    private val turnSystem: TurnSystem
) {
    fun createTroopsForCurrentTeam() {
        troopCreationSystem.createTroopsForTeam(turnSystem.currentTeam)
    }
}
