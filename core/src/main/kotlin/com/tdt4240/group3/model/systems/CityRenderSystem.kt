package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.assets.disposeSafely
import com.badlogic.gdx.utils.Disposable

class CityRenderSystem(
    private val batch: SpriteBatch,
    private val camera: OrthographicCamera
) : IteratingSystem(allOf(PositionComponent::class, CityComponent::class).get()), Disposable {

    private val cityTexture = Texture(Gdx.files.internal("Manchester_City_FC_badge.svg.png"))

    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined

        batch.use {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val pos = entity[PositionComponent.mapper] ?: return
        val drawX = pos.x - 32f
        val drawY = pos.y - 32f
        batch.draw(cityTexture, drawX, drawY, 64f, 64f)

    }

    override fun dispose() {
        cityTexture.disposeSafely()
    }
}
