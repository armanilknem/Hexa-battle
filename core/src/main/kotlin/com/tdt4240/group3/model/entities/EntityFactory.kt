package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Entity

interface EntityFactory<T> {
    fun createEntity(config: T) : Entity
}
