package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.components.TeamComponent

data class TeamVisuals(
    val territoryColor: Color,
    val troopTexture: Texture?
)

object TeamStyleRegistry : Disposable {
    private lateinit var visuals: Map<Pair<TeamComponent.TeamName, Int>, TeamVisuals>
    private var isInitialized = false

    fun init() {
        if (isInitialized) return

        visuals = mapOf(
            (TeamComponent.TeamName.RED to 20) to TeamVisuals(
                Color.RED,
                Texture(Gdx.files.internal("red_troop.png"))
            ),
            (TeamComponent.TeamName.RED to 40) to TeamVisuals(
                Color.RED,
                Texture(Gdx.files.internal("red_troop.png"))
            ),
            (TeamComponent.TeamName.BLUE to 20) to TeamVisuals(
                Color.BLUE,
                Texture(Gdx.files.internal("blue_troop.png"))
            ),
            (TeamComponent.TeamName.BLUE to 40) to TeamVisuals(
                Color.BLUE,
                Texture(Gdx.files.internal("blue_troop.png"))
            ),
        )
        isInitialized = true
    }

    fun get(team: TeamComponent.TeamName, strength: Int): TeamVisuals {
        if (!isInitialized) init()
        return visuals.getValue((team to strength))
    }

    override fun dispose() {
        if (!isInitialized) return
        visuals.values.forEach { it.troopTexture?.dispose() }
        isInitialized = false
    }
}
