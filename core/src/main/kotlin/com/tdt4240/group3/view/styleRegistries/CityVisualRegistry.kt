package com.tdt4240.group3.view.styleRegistries

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.TeamComponent
import ktx.ashley.get

data class CityVisuals(
    val texture: Texture,
    val width: Float,
    val height: Float,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f,
)

private data class CitySize(
    val width: Float,
    val height: Float,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f,
)

private val CAPITAL_SIZE = CitySize(width = 40f, height = 44f, xOffset = 1f, yOffset = 5.5f)
private val NORMAL_SIZE = CitySize(width = 39f, height = 40f, xOffset = 0f, yOffset = 2f)

object CityStyleRegistry : Disposable {
    // Both neutral variants share the same texture; only their draw sizes differ.
    private val neutralTexture = Texture(Gdx.files.internal("normalCities/NormalCity.png"))

    private val neutralCapitalVisual = CityVisuals(
        texture = neutralTexture,
        width = CAPITAL_SIZE.width,
        height = CAPITAL_SIZE.height,
        xOffset = CAPITAL_SIZE.xOffset,
        yOffset = CAPITAL_SIZE.yOffset,
    )

    private val neutralCityVisual = CityVisuals(
        texture = neutralTexture,
        width = NORMAL_SIZE.width,
        height = NORMAL_SIZE.height,
        xOffset = NORMAL_SIZE.xOffset,
        yOffset = NORMAL_SIZE.yOffset,
    )

    fun getFor(entity: Entity): CityVisuals {
        val team = entity[TeamComponent.mapper]?.team ?: Team.NONE
        val teamVisuals = TeamVisualRegistry.visuals[team]
        val isCapital = entity[CapitalComponent.mapper] != null

        return if (isCapital) {
            teamVisuals?.let {
                CityVisuals(
                    texture = it.capitalTexture,
                    width = CAPITAL_SIZE.width,
                    height = CAPITAL_SIZE.height,
                    xOffset = CAPITAL_SIZE.xOffset,
                    yOffset = CAPITAL_SIZE.yOffset,
                )
            } ?: neutralCapitalVisual
        } else {
            teamVisuals?.let {
                CityVisuals(
                    texture = it.cityTexture,
                    width = NORMAL_SIZE.width,
                    height = NORMAL_SIZE.height,
                    xOffset = NORMAL_SIZE.xOffset,
                    yOffset = NORMAL_SIZE.yOffset,
                )
            } ?: neutralCityVisual
        }
    }

    override fun dispose() {
        neutralTexture.dispose()
    }
}
