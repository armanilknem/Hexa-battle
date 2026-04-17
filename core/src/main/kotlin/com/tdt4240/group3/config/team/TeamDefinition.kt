package com.tdt4240.group3.config.team

data class TeamDefinition(
    val key: String,
    val displayName: String,
    val territoryColorHex: String,
    val troopTextures: Map<UnitTier, String>
)
