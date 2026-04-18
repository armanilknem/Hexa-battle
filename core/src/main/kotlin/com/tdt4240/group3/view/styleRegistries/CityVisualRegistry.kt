package com.tdt4240.group3.view.styleRegistries

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.components.CapitalComponent

data class CityVisuals(
    val texture: Texture,
    val width: Float,
    val height: Float,
    val xOffset: Float,
    val yOffset: Float
)

object CityStyleRegistry : Disposable {
    private lateinit var capitalVisuals: CityVisuals
    private lateinit var normalVisuals: CityVisuals
    private var initialized = false

    fun init() {
        if (initialized) return

        capitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("CapitalCity.png")),
            width = 42f,
            height = 42f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        normalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("NormalCity.png")),
            width = 68.5f,
            height = 72f,
            xOffset = 0f,
            yOffset = 1.5f
        )

        initialized = true
    }

    fun getFor(entity: Entity): CityVisuals {
        if (!initialized) init()

        return if (entity.getComponent(CapitalComponent::class.java) != null) {
            capitalVisuals
        } else {
            normalVisuals
        }
    }

    override fun dispose() {
        if (!initialized) return
        capitalVisuals.texture.dispose()
        normalVisuals.texture.dispose()
        initialized = false
    }
}
