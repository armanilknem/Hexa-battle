package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class CapitalComponent: Component, Pool.Poolable {
    override fun reset() = Unit
}
