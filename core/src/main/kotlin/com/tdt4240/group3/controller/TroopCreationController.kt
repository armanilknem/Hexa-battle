package com.tdt4240.group3.controller

import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.systems.TroopCreationSystem
import com.tdt4240.group3.model.team.TeamName

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
) {
    fun createTroopsForTeam(team: TeamName) {
        troopCreationSystem.createTroopsForTeam(team)
    }
}
