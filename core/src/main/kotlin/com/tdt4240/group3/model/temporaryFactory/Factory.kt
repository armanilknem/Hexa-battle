package com.tdt4240.group3.model.temporaryFactory

import com.badlogic.ashley.core.Entity

interface Factory<T> {
    fun createEntity(config: T) : Entity
}
