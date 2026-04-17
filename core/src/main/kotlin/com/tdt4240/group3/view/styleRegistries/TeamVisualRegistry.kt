package com.tdt4240.group3.view.styleRegistries

import com.badlogic.gdx.graphics.Color
import com.tdt4240.group3.config.team.TeamCatalog
import com.tdt4240.group3.model.components.TeamComponent

object TeamVisualRegistry {
    private val catalog = TeamCatalog.teams

    fun getTerritoryColor(team: TeamComponent.TeamName): Color {
        val teamDefinition = catalog.getValue(team.toConfigKey())
        return Color.valueOf(teamDefinition.territoryColorHex)
    }

    private fun TeamComponent.TeamName.toConfigKey(): String {
        require(this != TeamComponent.TeamName.NONE) { "Neutral team has no visual configuration." }
        return name.lowercase()
    }
}
