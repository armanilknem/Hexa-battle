package com.tdt4240.group3.model.ecs.entities

import com.badlogic.ashley.core.Entity

interface Factory<T> {
    fun createEntity(config: T) : Entity
}
