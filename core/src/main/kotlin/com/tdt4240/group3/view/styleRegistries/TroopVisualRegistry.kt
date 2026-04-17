package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.config.team.TeamCatalog
import com.tdt4240.group3.config.team.UnitTier
import com.tdt4240.group3.model.components.TeamComponent

object TroopVisualRegistry : Disposable {
    private val catalog = TeamCatalog.teams
    private val textures = mutableMapOf<Pair<String, UnitTier>, Texture>()

    private var initialized = false
    fun init() {
        if (initialized) return

        catalog.values.forEach { teamDef ->
            teamDef.troopTextures.forEach { (tier, path) ->
                textures[teamDef.key to tier] = Texture(Gdx.files.internal(path))
            }
        }
        initialized = true
    }

    fun getTexture(team: TeamComponent.TeamName, strength: Int): Texture {
        if (!initialized) init()
        val tier = tierFor(strength)
        val key = team.toConfigKey()
        return textures[key to tier]
            ?: throw IllegalArgumentException("No texture found for team $team and tier $tier")
    }

    private fun tierFor(strength: Int): UnitTier {
        return if (strength < 65) UnitTier.TIER_1 else UnitTier.TIER_2
    }

    override fun dispose() {
        if (!initialized) return
        textures.values.forEach { it.dispose() }
        textures.clear()
        initialized = false
    }

    private fun TeamComponent.TeamName.toConfigKey(): String {
        require(this != TeamComponent.TeamName.NONE) { "Neutral team has no troop textures." }
        return name.lowercase()
    }
}
