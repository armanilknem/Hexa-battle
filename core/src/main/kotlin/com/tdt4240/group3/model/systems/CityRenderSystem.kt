package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.disposeSafely
import com.badlogic.gdx.utils.Disposable

class CityRenderSystem(
    private val batch: SpriteBatch
) : IteratingSystem(allOf(PositionComponent::class, CityComponent::class).get()), Disposable {

    private val cityTexture = Texture(Gdx.files.internal("Manchester_City_FC_badge.svg.png"))

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val pos = entity[PositionComponent.mapper]

        if (pos != null) {
            batch.draw(cityTexture, pos.x.toFloat(), pos.y.toFloat(), 64f, 64f)
        }
    }

    override fun dispose() {
        cityTexture.disposeSafely()
    }
}
