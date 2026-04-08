package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.entities.EntityFactory




class TroopCreationSystem(private val engine: Engine, TEAM: TeamComponent.TeamName, STRENGTH: Int, Q: Int, R: Int) : EntitySystem() {

     private val factory = EntityFactory(engine)
    init{
        factory.createTroop(,0,0,00,)
    }

}
