package com.tdt4240.group3.controller

import com.tdt4240.group3.model.ecs.systems.TurnSystem

class TurnController(
    private val turnSystem: TurnSystem,
) {
    fun endTurn() {
        turnSystem.endTurn()
    }
}
