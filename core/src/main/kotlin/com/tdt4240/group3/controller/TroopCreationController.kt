package com.tdt4240.group3.controller

import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.ecs.systems.TroopCreationSystem

class TroopCreationController(
    private val troopCreationSystem: TroopCreationSystem,
) {
    fun createTroopsForTeam(team: TeamComponent.TeamName) {
        troopCreationSystem.createTroopsForTeam(team)
    }
}
