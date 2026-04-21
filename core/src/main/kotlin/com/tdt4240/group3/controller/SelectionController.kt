package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.hexmap.HexMapQueries
import com.tdt4240.group3.model.components.TouchInputComponent
import ktx.ashley.entity
import ktx.ashley.with

class SelectionController(private val engine: Engine) {
    fun handleTouch(worldX: Float, worldY: Float) {
        engine.entity {
            with<TouchInputComponent> {
                x = worldX
                y = worldY
            }
        }
    }

    fun findTileAt(worldX: Float, worldY: Float) = HexMapQueries.findTileAt(engine, worldX, worldY)
    fun findCityAt(worldX: Float, worldY: Float) = HexMapQueries.findCityAtByXY(engine, worldX, worldY)
}
