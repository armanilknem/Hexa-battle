package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.tdt4240.group3.model.Team
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable

/**
 * Container for visual assets and properties associated with a [Team].
 *
 * @property territoryColor The primary color used to represent this team's territory on the map.
 * @property textures A map of [UnitTier] to the corresponding [Texture] for this team's units.
 */
data class TeamVisuals(
    val territoryColor: Color,
    val textures: Map<UnitTier, Texture>
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

/**
 * Registry for team-related visual properties.
 * Provides centralized access to colors and troop textures defined for each [Team].
 *
 * This registry handles the mapping of strength values to [UnitTier]s to determine
 * which texture to display for a given unit.
 */
object TeamVisualRegistry : Disposable {

    val visuals = mapOf(
        Team.RED to TeamVisuals(
            territoryColor = Color.valueOf("#FF0000"),
            textures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/red_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/red_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/red_plane.png")),
            )
        ),
        Team.BLUE to TeamVisuals(
            territoryColor = Color.valueOf("#0000FF"),
            textures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/blue_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/blue_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/blue_plane.png")),
            )
        ),
        Team.PURPLE to TeamVisuals(
            territoryColor = Color.valueOf("#FF00FF"),
            textures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/purple_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/purple_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/purple_plane.png")),
            )
        ),
        Team.GREEN to TeamVisuals(
            territoryColor = Color.valueOf("#00FF00"),
            textures = mapOf(
                UnitTier.TIER_1 to Texture(Gdx.files.internal("troopSprites/green_soldier.png")),
                UnitTier.TIER_2 to Texture(Gdx.files.internal("troopSprites/green_tank.png")),
                UnitTier.TIER_3 to Texture(Gdx.files.internal("troopSprites/green_plane.png")),
            )
        )
    )

    /**
     * Returns the territory color for the given [team].
     */
    fun getColor(team: Team): Color {
        return visuals[team]?.territoryColor ?: Color.WHITE
    }

    /**
     * Returns the appropriate unit [Texture] based on the [team] and the unit's [strength].
     *
     * @throws IllegalArgumentException if no texture is found for the calculated tier.
     */
    fun getTexture(team: Team, strength: Int): Texture {
        val tier = tierFor(strength)
        return visuals[team]?.textures?.get(tier)
            ?: throw IllegalArgumentException("No texture found for team $team and tier $tier")
    }

    /**
     * Maps unit strength to a [UnitTier].
     */
    private fun tierFor(strength: Int): UnitTier {
        return when {
            strength < 40 -> UnitTier.TIER_1
            strength < 80 -> UnitTier.TIER_2
            else -> UnitTier.TIER_3
        }
    }

    override fun dispose() {
        visuals.values.forEach {
            it.textures.values.forEach { texture ->
                texture.dispose()
            }
        }
    }
}
