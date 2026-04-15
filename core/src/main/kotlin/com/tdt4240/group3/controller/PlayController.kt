package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.CollisionSystem
import com.tdt4240.group3.model.systems.MovementSystem
import com.tdt4240.group3.model.systems.SelectionSystem
import com.tdt4240.group3.model.systems.TerritorySystem
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TroopHighlightSystem
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.view.screens.PlayScreen

class PlayController(
    private val game: Hexa_Battle,
    private val engine: Engine,
) {
    private val factory: EntityFactory = EntityFactory(engine)

    fun createScreen(): PlayScreen {
        val turnSystem = TurnSystem()
        val selectionSystem = SelectionSystem()
        val movementSystem = MovementSystem()
        val collisionSystem = CollisionSystem()
        val troopCreationSystem = TroopCreationSystem(engine)
        val territorySystem = TerritorySystem()
        val troopHighlightSystem = TroopHighlightSystem(turnSystem)

        this.setUpSystems(turnSystem, selectionSystem, movementSystem, collisionSystem, troopCreationSystem, territorySystem, troopHighlightSystem)

        val turnController = TurnController(turnSystem)
        val troopCreationController = TroopCreationController(troopCreationSystem)
        val pauseController = PauseController()
        val selectionController = SelectionController(selectionSystem)

        this.setUpInitialGameState()
        this.setUpWorld()
        this.initializeCities()
        this.initializeTroops(troopCreationController)

        return PlayScreen(game, engine, turnController, pauseController, selectionController)
    }

    private fun setUpSystems(turnSystem: TurnSystem, selectionSystem: SelectionSystem, movementSystem: MovementSystem, collisionSystem: CollisionSystem, troopCreationSystem: TroopCreationSystem, territorySystem: TerritorySystem, troopHighlightSystem: TroopHighlightSystem) {
        engine.addSystem(turnSystem)
        engine.addSystem(selectionSystem)
        engine.addSystem(movementSystem)
        engine.addSystem(collisionSystem)
        engine.addSystem(troopCreationSystem)
        engine.addSystem(territorySystem)
        engine.addSystem(troopHighlightSystem)
    }

    private fun setUpWorld() {
        factory.generateRectangularGrid(18, 15)
    }

    private fun initializeCities() {
        factory.createCity(
            name = "Oslo", isCapital = true, baseProduction = 20,
            q = 1, r = 2, team = TeamComponent.TeamName.RED
        )
        factory.createCity(
            name = "Bergen", isCapital = true, baseProduction = 20,
            q = 10, r = 11, team = TeamComponent.TeamName.BLUE
        )
        factory.generateNormalCities(
            count = 20,
            capitalPositions = listOf(Pair(1, 2), Pair(10, 11))
        )
    }

    private fun setUpInitialGameState() {
        factory.createGameState()
    }

    private fun initializeTroops(troopCreationController: TroopCreationController) {
        troopCreationController.createTroopsForTeam(TeamComponent.TeamName.BLUE)
    }
}
