package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.Team
import ktx.ashley.entity
import ktx.ashley.with

class TileFactory(private val engine: Engine): EntityFactory<TileConfig> {
    override fun createEntity(config: TileConfig) = engine.entity {
        with<TileComponent> {
            this.type = config.type
        }
        with<PositionComponent> {
            this.q = config.q
            this.r = config.r
            this.zIndex = 0 // Bottom layer //TODO("should be changed to some sort of global variable for better clarity")
        }
        with<TeamComponent> {
            this.team = Team.NONE
        }
    }
}
