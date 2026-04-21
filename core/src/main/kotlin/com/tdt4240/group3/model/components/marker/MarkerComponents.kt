package com.tdt4240.group3.model.components.marker

import com.badlogic.ashley.core.Component
import ktx.ashley.mapperFor

/** Added to a troop or tile that the current player can interact with. */
class SelectableComponent : Component {
    companion object { val mapper = mapperFor<SelectableComponent>() }
}

/** Added to the troop the current player has tapped/clicked. */
class SelectedComponent : Component {
    companion object { val mapper = mapperFor<SelectedComponent>() }
}

/** Added to tiles within movement range of the selected troop. */
class HighlightedComponent : Component {
    companion object { val mapper = mapperFor<HighlightedComponent>() }
}

/** Added to a troop while it is mid-move and has not yet resolved its destination. */
class CollidingComponent : Component {
    companion object { val mapper = mapperFor<CollidingComponent>() }
}

/** Added to a city entity to mark it as part of the current team's territory. */
class TerritoryComponent : Component {
    companion object { val mapper = mapperFor<TerritoryComponent>() }
}

/** Added to the game-state entity to signal that TroopCreationSystem should run this frame. */
class NeedsTroopSpawnComponent : Component {
    companion object { val mapper = mapperFor<NeedsTroopSpawnComponent>() }
}
