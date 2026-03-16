package com.tdt4240.group3.model.entities

import com.tdt4240.group3.model.components.Component

class EntityManager {
    private var nextId = 0
    private val entityComponents = mutableMapOf<Int, MutableList<Component>>()

    fun createEntity(): Entity {
        val entity = Entity(nextId++)
        entityComponents[entity.id] = mutableListOf()
        return entity
    }

    fun addComponent(entity: Entity, component: Component) {
        entityComponents[entity.id]?.add(component)
    }

    fun getAllEntitiesWithComponents(): Map<Int, List<Component>> {
        return entityComponents
    }

    fun <T : Component> getComponent(entityId: Int, type: Class<T>): T? {
        return entityComponents[entityId]?.filterIsInstance(type)?.firstOrNull()
    }

}
