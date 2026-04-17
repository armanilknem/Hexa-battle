package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.graphics.Color
import com.tdt4240.group3.model.team.TeamCatalog
import com.tdt4240.group3.model.team.TeamName

/**
 * Registry for team-related visual properties.
 * Provides access to colors and other visual configurations defined in the TeamCatalog.
 */
object TeamVisualRegistry {
    private val catalog = TeamCatalog.teams

    fun getTerritoryColor(team: TeamName): Color {
        if (team == TeamName.NONE) return Color.CLEAR

        // Find the definition using the enum name as lowercase key (matching the catalog)
        val teamDefinition = catalog[team.name.lowercase()]
            ?: throw IllegalArgumentException("No visual configuration found for team: $team")

        return Color.valueOf(teamDefinition.territoryColorHex)
    }
}
