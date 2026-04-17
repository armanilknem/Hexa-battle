package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.team.TeamCatalog
import com.tdt4240.group3.model.team.UnitTier
import com.tdt4240.group3.model.team.TeamName

/**
 * Registry for troop-related textures.
 * Manages loading and providing access to unit textures based on team and strength tier.
 */
object TroopVisualRegistry : Disposable {
    private val catalog = TeamCatalog.teams
    private val textures = mutableMapOf<Pair<TeamName, UnitTier>, Texture>()

    private var initialized = false
    fun init() {
        if (initialized) return

        catalog.values.forEach { teamDef ->
            teamDef.troopTextures.forEach { (tier, path) ->
                textures[teamDef.teamName to tier] = Texture(Gdx.files.internal(path))
            }
        }
        initialized = true
    }

    fun getTexture(teamName: TeamName, strength: Int): Texture {
        if (!initialized) init()
        val tier = tierFor(strength)
        return textures[teamName to tier]
            ?: throw IllegalArgumentException("No texture found for team $teamName and tier $tier")
    }

    private fun tierFor(strength: Int): UnitTier {
        return when {
            strength < 40 -> UnitTier.TIER_1
            strength < 80 -> UnitTier.TIER_2
            else -> UnitTier.TIER_3
        }
    }

    override fun dispose() {
        if (!initialized) return
        textures.values.forEach { it.dispose() }
        textures.clear()
        initialized = false
    }
}
