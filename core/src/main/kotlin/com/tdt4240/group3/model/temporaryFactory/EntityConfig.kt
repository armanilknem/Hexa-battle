package com.tdt4240.group3.model.temporaryFactory

import com.tdt4240.group3.model.ecs.components.TileComponent
import com.tdt4240.group3.model.team.TeamName

data class PlayerConfig(
    val name: String
)

data class TroopConfig(
    val team: TeamName,
    val unitKey: String,
    val strength: Int,
    val q: Int,
    val r: Int
)

data class CityConfig(
    val name: String,
    val baseProduction: Int,
    val q: Int,
    val r: Int,
    val team: TeamName
)

data class CapitalConfig(
    val name: String,
    val baseProduction: Int,
    val q: Int,
    val r: Int,
    val team: TeamName
)

data class TileConfig(
    val q: Int,
    val r: Int,
    val type: TileComponent.TileType //TODO("should look into if we can find a better way to find out Tile type")
)

data class GameStateConfig(
    val activeTeams: List<TeamName>
)
