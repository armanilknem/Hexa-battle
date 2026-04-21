package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.Team

data class TroopConfig(
    val team: Team,
    val strength: Int,
    val q: Int,
    val r: Int
)

data class CityConfig(
    val name: String,
    val baseProduction: Int,
    val q: Int,
    val r: Int,
    val team: Team
)

data class CapitalConfig(
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
