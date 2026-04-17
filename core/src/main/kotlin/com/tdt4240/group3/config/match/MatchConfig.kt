package com.tdt4240.group3.config.match

import com.tdt4240.group3.model.components.TeamComponent

data class MatchConfig(
    val activeTeams: List<TeamComponent.TeamName>
)
