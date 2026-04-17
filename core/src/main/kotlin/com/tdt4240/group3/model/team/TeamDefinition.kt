package com.tdt4240.group3.model.team

data class TeamDefinition(
    val teamName: TeamName,
    val displayName: String,
    val territoryColorHex: String,
    val troopTextures: Map<UnitTier, String>
)
