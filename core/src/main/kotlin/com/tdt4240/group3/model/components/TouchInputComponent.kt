package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/**
 * One-shot command entity: [com.tdt4240.group3.controller.SelectionController] creates an entity
 * carrying this component with the world-space tap coordinates; [com.tdt4240.group3.model.systems.SelectionSystem]
 * reads it, executes the selection logic, then removes the entity from the engine.
 */
class TouchInputComponent : Component {
    var x: Float = 0f
    var y: Float = 0f

    companion object { val mapper = mapperFor<TouchInputComponent>() }
}
