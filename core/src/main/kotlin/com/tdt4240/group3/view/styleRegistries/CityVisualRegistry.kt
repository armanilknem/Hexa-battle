package com.tdt4240.group3.view.styleRegistries

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.ecs.components.TeamComponent
import com.tdt4240.group3.model.team.TeamName
import ktx.ashley.get

data class CityVisuals(
    val texture: Texture,
    val width: Float,
    val height: Float,
    val xOffset: Float,
    val yOffset: Float
)

object CityStyleRegistry : Disposable {
    private lateinit var neutralCapitalVisuals: CityVisuals
    private lateinit var neutralNormalVisuals: CityVisuals
    private lateinit var redCapitalVisuals: CityVisuals
    private lateinit var blueCapitalVisuals: CityVisuals
    private lateinit var greenCapitalVisuals: CityVisuals
    private lateinit var purpleCapitalVisuals: CityVisuals
    private lateinit var redNormalVisuals: CityVisuals
    private lateinit var blueNormalVisuals: CityVisuals
    private lateinit var greenNormalVisuals: CityVisuals
    private lateinit var purpleNormalVisuals: CityVisuals
    private var initialized = false

    fun init() {
        if (initialized) return

        // Below can be used for when a player takes over another players capital
        neutralCapitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("CapitalCity.png")),
            width = 40f,
            height = 44f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        redCapitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("capitals/RedCapital.png")),
            width = 40f,
            height = 44f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        blueCapitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("capitals/BlueCapital.png")),
            width = 40f,
            height = 44f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        greenCapitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("capitals/GreenCapital.png")),
            width = 40f,
            height = 44f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        purpleCapitalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("capitals/PurpleCapital.png")),
            width = 40f,
            height = 44f,
            xOffset = 1f,
            yOffset = 5.5f
        )

        neutralNormalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("normalCities/NormalCity.png")),
            width = 39f,
            height = 40f,
            xOffset = 0f,
            yOffset = 2f
        )

        redNormalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("normalCities/RedCity.png")),
            width = 39f,
            height = 40f,
            xOffset = 0f,
            yOffset = 2f
        )

        blueNormalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("normalCities/BlueCity.png")),
            width = 39f,
            height = 40f,
            xOffset = 0f,
            yOffset = 2f
        )

        greenNormalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("normalCities/GreenCity.png")),
            width = 39f,
            height = 40f,
            xOffset = 0f,
            yOffset = 2f
        )

        purpleNormalVisuals = CityVisuals(
            texture = Texture(Gdx.files.internal("normalCities/PurpleCity.png")),
            width = 39f,
            height = 40f,
            xOffset = 0f,
            yOffset = 2f
        )

        initialized = true
    }

    fun getFor(entity: Entity): CityVisuals {
        if (!initialized) init()

        val team = entity[TeamComponent.mapper]?.team ?: TeamName.NONE

        return if (entity.getComponent(CapitalComponent::class.java) != null) {
            getCapitalVisuals(team)
        } else {
            getNormalVisuals(team)
        }
    }

    private fun getCapitalVisuals(team: TeamName): CityVisuals = when (team) {
        TeamName.RED -> redCapitalVisuals
        TeamName.BLUE -> blueCapitalVisuals
        TeamName.GREEN -> greenCapitalVisuals
        TeamName.PURPLE -> purpleCapitalVisuals
        TeamName.NONE -> neutralCapitalVisuals
    }

    private fun getNormalVisuals(team: TeamName): CityVisuals = when (team) {
        TeamName.RED -> redNormalVisuals
        TeamName.BLUE -> blueNormalVisuals
        TeamName.GREEN -> greenNormalVisuals
        TeamName.PURPLE -> purpleNormalVisuals
        TeamName.NONE -> neutralNormalVisuals
    }

    override fun dispose() {
        if (!initialized) return
        neutralCapitalVisuals.texture.dispose()
        neutralNormalVisuals.texture.dispose()
        redCapitalVisuals.texture.dispose()
        blueCapitalVisuals.texture.dispose()
        greenCapitalVisuals.texture.dispose()
        purpleCapitalVisuals.texture.dispose()
        redNormalVisuals.texture.dispose()
        blueNormalVisuals.texture.dispose()
        greenNormalVisuals.texture.dispose()
        purpleNormalVisuals.texture.dispose()
        initialized = false
    }
}
