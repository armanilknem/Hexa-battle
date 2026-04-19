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
private val NORMAL_SIZE  = CitySize(width = 39f, height = 40f, xOffset = 0f, yOffset = 2f)

// Legg til nytt team: én linje her og én i NORMAL_PATHS
private val CAPITAL_PATHS: Map<TeamName, String> = mapOf(
    TeamName.NONE   to "normalCities/NormalCity.png", // midlertidig fallback
    TeamName.RED    to "capitals/RedCapital.png",
    TeamName.BLUE   to "capitals/BlueCapital.png",
    TeamName.GREEN  to "capitals/GreenCapital.png",
    TeamName.PURPLE to "capitals/PurpleCapital.png",
)

private val NORMAL_PATHS: Map<TeamName, String> = mapOf(
    TeamName.NONE   to "normalCities/NormalCity.png",
    TeamName.RED    to "normalCities/RedCity.png",
    TeamName.BLUE   to "normalCities/BlueCity.png",
    TeamName.GREEN  to "normalCities/GreenCity.png",
    TeamName.PURPLE to "normalCities/PurpleCity.png",
)

object CityStyleRegistry : Disposable {
    private val capitalVisuals = mutableMapOf<TeamName, CityVisuals>()
    private val normalVisuals  = mutableMapOf<TeamName, CityVisuals>()
    private var initialized = false

    fun init() {
        if (initialized) return

        fun loadAll(paths: Map<TeamName, String>, size: CitySize): Map<TeamName, CityVisuals> =
            paths.mapValues { (_, path) ->
                CityVisuals(
                    texture = Texture(Gdx.files.internal(path)),
                    width   = size.width,
                    height  = size.height,
                    xOffset = size.xOffset,
                    yOffset = size.yOffset,
                )
            }

        capitalVisuals.putAll(loadAll(CAPITAL_PATHS, CAPITAL_SIZE))
        normalVisuals.putAll(loadAll(NORMAL_PATHS, NORMAL_SIZE))
        initialized = true
    }

    fun getFor(entity: Entity): CityVisuals {
        if (!initialized) init()
        val team = entity[TeamComponent.mapper]?.team ?: TeamName.NONE
        val map  = if (entity.getComponent(CapitalComponent::class.java) != null)
            capitalVisuals else normalVisuals
        return map[team] ?: error("Ingen CityVisuals registrert for team: $team")
    }

    override fun dispose() {
        if (!initialized) return
        (capitalVisuals.values + normalVisuals.values).forEach { it.texture.dispose() }
        capitalVisuals.clear()
        normalVisuals.clear()
        initialized = false
    }
}
