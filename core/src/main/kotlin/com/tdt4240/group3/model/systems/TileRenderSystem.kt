package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TileComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class TileRenderSystem(private val shapeRenderer: ShapeRenderer, private val camera: OrthographicCamera)
    : IteratingSystem(allOf(PositionComponent::class, TileComponent::class).get()) {

    override fun update(deltaTime: Float) {
        shapeRenderer.projectionMatrix = camera.combined
        // Use .use to automatically handle begin() and end()
        shapeRenderer.use(ShapeRenderer.ShapeType.Line) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val pos = entity[PositionComponent.mapper] ?: return
        drawHexagon(pos.x.toFloat(), pos.y.toFloat(), 32f)
    }

    private fun drawHexagon(x: Float, y: Float, size: Float) {
        val vertices = 6
        for (i in 0 until vertices) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)
            shapeRenderer.line(
                x + size * cos(angle1).toFloat(),
                y + size * sin(angle1).toFloat(),
                x + size * cos(angle2).toFloat(),
                y + size * sin(angle2).toFloat()
            )
        }
    }
}
