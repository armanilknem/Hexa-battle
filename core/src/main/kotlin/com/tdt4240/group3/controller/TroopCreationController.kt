package com.tdt4240.group3.controller

import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.team.TeamName

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
) {
    fun createTroopsForTeam(team: TeamName) {
        troopCreationSystem.createTroopsForTeam(team)
    }
}
