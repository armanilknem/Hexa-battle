package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.PlayerComponent

class EntityFactory(private val entityManager: EntityManager) {

    fun createPlayer(name: String, score: Int) : Entity {
        val entity = entityManager.createEntity()
        val playerComponent = PlayerComponent(name, score)
        entityManager.addComponent(entity, playerComponent)
        return entity
    }

    // fun createTroop here

    // fun createTile here

    // fun createCity here
}
