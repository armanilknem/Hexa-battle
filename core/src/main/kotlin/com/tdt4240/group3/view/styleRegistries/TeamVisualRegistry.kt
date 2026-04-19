package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.Team

/**
 * Container for visual assets and properties associated with a [Team].
 *
 * All team-owned assets live here so adding or changing a team only requires
 * updating one registry.
 */
data class TeamVisuals(
    val territoryColor: Color,
    val troopTextures: Map<UnitTier, Texture>,
    val backgrounds: Map<BackgroundTier, Texture>,
    val capitalTexture: Texture,
    val cityTexture: Texture,
)

/**
 * Represents visual tiers for units based on their strength.
 *
 * Note: This is only a visual representation used for selecting the appropriate texture
 * and does not affect gameplay mechanics directly.
 */
enum class UnitTier {
    TIER_1,
    TIER_2,
    TIER_3
}

enum class BackgroundTier {
    DEFEAT,
    WIN,
}

/**
 * Registry for team-related visual properties.
 * Provides centralized access to colors, troop textures, city visuals, and win backgrounds.
 */
object TeamVisualRegistry : Disposable {

    val visuals = mapOf(
        Team.RED to TeamVisuals(
            territoryColor = Color.valueOf("#FF0000"),
            troopTextures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/red_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/red_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/red_plane.png")),
            ),
            backgrounds = mapOf(
                BackgroundTier.DEFEAT to Texture(Gdx.files.internal("backgrounds/DefeatBackground.png")),
                BackgroundTier.WIN to Texture(Gdx.files.internal("backgrounds/RedWinBackground.png")),
            ),
            capitalTexture = Texture(Gdx.files.internal("capitals/RedCapital.png")),
            cityTexture = Texture(Gdx.files.internal("normalCities/RedCity.png")),
        ),
        Team.BLUE to TeamVisuals(
            territoryColor = Color.valueOf("#0000FF"),
            troopTextures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/blue_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/blue_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/blue_plane.png")),
            ),
            backgrounds = mapOf(
                BackgroundTier.DEFEAT to Texture(Gdx.files.internal("backgrounds/DefeatBackground.png")),
                BackgroundTier.WIN to Texture(Gdx.files.internal("backgrounds/BlueWinBackground.png")),
            ),
            capitalTexture = Texture(Gdx.files.internal("capitals/BlueCapital.png")),
            cityTexture = Texture(Gdx.files.internal("normalCities/BlueCity.png")),
        ),
        Team.PURPLE to TeamVisuals(
            territoryColor = Color.valueOf("#FF00FF"),
            troopTextures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/purple_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/purple_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/purple_plane.png")),
            ),
            backgrounds = mapOf(
                BackgroundTier.DEFEAT to Texture(Gdx.files.internal("backgrounds/DefeatBackground.png")),
                BackgroundTier.WIN to Texture(Gdx.files.internal("backgrounds/PurpleWinBackground.png")),
            ),
            capitalTexture = Texture(Gdx.files.internal("capitals/PurpleCapital.png")),
            cityTexture = Texture(Gdx.files.internal("normalCities/PurpleCity.png")),
        ),
        Team.GREEN to TeamVisuals(
            territoryColor = Color.valueOf("#00FF00"),
            troopTextures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/green_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/green_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/green_plane.png")),
            ),
            backgrounds = mapOf(
                BackgroundTier.DEFEAT to Texture(Gdx.files.internal("backgrounds/DefeatBackground.png")),
                BackgroundTier.WIN to Texture(Gdx.files.internal("backgrounds/GreenWinBackground.png")),
            ),
            capitalTexture = Texture(Gdx.files.internal("capitals/GreenCapital.png")),
            cityTexture = Texture(Gdx.files.internal("normalCities/GreenCity.png")),
        ),
    )

    fun getColor(team: Team): Color {
        return visuals[team]?.territoryColor ?: Color.WHITE
    }

    fun getTexture(team: Team, strength: Int): Texture {
        val tier = tierFor(strength)
        return visuals[team]?.troopTextures?.get(tier)
            ?: throw IllegalArgumentException("No texture found for team $team and tier $tier")
    }

    private fun tierFor(strength: Int): UnitTier {
        return when {
            strength < 40 -> UnitTier.TIER_1
            strength < 80 -> UnitTier.TIER_2
            else -> UnitTier.TIER_3
        }
    }

    override fun dispose() {
        visuals.values.forEach { teamVisuals ->
            teamVisuals.troopTextures.values.forEach { it.dispose() }
            teamVisuals.backgrounds.values.forEach { it.dispose() }
            teamVisuals.capitalTexture.dispose()
            teamVisuals.cityTexture.dispose()
        }
    }
}
