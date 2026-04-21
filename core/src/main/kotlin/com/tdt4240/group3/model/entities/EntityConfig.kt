package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.TileComponent

data class TroopConfig(
    val team: Team,
    val strength: Int,
    val q: Int,
    val r: Int
)

/** Config for both regular cities and capitals — [com.tdt4240.group3.model.entities.CapitalFactory]
 *  distinguishes a capital by also attaching a [com.tdt4240.group3.model.components.CapitalComponent]. */
data class CityConfig(
    val name: String,
    val baseProduction: Int,
    val q: Int,
    val r: Int,
    val team: Team
)

data class TileConfig(
    val q: Int,
    val r: Int,
    val type: TileComponent.TileType
)

data class GameStateConfig(
    val activeTeams: List<Team>
)
