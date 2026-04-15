package com.tdt4240.group3.controller

import com.tdt4240.group3.model.systems.SelectionSystem

class SelectionController(
    private val selectionSystem: SelectionSystem,
) {
    fun handleTouch(x: Float, y: Float) {
        selectionSystem.handleTouch(x, y)
    }
}
