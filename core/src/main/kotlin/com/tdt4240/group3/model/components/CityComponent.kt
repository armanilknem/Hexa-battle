package com.tdt4240.group3.model.components

import com.badlogic.ashley.core.Component
import com.tdt4240.group3.config.GameConstants
import ktx.ashley.mapperFor

/** Identifies a city entity. [baseProduction] troops are added to the owning team each turn. */
class CityComponent : Component {
    var name: String = ""
    var baseProduction: Int = GameConstants.CITY_PRODUCTION

    companion object { val mapper = mapperFor<CityComponent>() }
}
