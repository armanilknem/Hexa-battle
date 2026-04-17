package com.tdt4240.group3.controller

import com.tdt4240.group3.model.ecs.systems.SelectionSystem

class SelectionController(private val engine: Engine) {
    fun handleTouch(worldX: Float, worldY: Float) {
        engine.entity {
            with<TouchInputComponent> {
                x = worldX
                y = worldY
            }
        }
    }

    fun findTileAt(worldX: Float, worldY: Float) = HexMapService.findTileAt(engine, worldX, worldY)
    fun findCityAt(worldX: Float, worldY: Float) = HexMapService.findCityAt(engine, worldX, worldY)
}
