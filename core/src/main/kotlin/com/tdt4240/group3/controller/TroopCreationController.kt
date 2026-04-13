package com.tdt4240.group3.controller

import com.tdt4240.group3.controller.systems.TroopCreationSystem
import com.tdt4240.group3.controller.systems.TurnSystem

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
    private val turnSystem: TurnSystem
) {
    fun createTroopsForCurrentTeam() {
        troopCreationSystem.createTroopsForTeam(turnSystem.currentTeam)
    }
}
