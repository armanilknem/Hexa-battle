package com.tdt4240.group3.controller

import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.Team

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
) {
    fun createTroopsForTeam(team: Team) {
        troopCreationSystem.createTroopsForTeam(team)
    }
}
