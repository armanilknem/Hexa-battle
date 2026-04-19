package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.UnitType

data class PlayerConfig(
    val name: String
)

data class TroopConfig(
    val team: Team,
    val unitType: UnitType,
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
    val type: TileComponent.TileType //TODO("should look into if we can find a better way to find out Tile type")
)

data class GameStateConfig(
    val activeTeams: List<Team>
)
