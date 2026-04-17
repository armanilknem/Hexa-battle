package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.ecs.components.GameStateComponent
import com.tdt4240.group3.model.ecs.entities.EntityFactory
import com.tdt4240.group3.model.ecs.systems.*
import com.tdt4240.group3.model.team.TeamName
import com.tdt4240.group3.model.systems.WinSystem
import com.tdt4240.group3.view.screens.PlayScreen
import ktx.ashley.get

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
        val winSystem = WinSystem()

        this.setUpSystems(turnSystem, selectionSystem, movementSystem, collisionSystem, troopCreationSystem, territorySystem, troopHighlightSystem, winSystem)

        val turnController = TurnController(turnSystem)
        val troopCreationController = TroopCreationController(troopCreationSystem)
        val pauseController = PauseController()
        val selectionController = SelectionController(engine)

        val gameStateEntity = this.setUpInitialGameState()
        this.setUpWorld()
        this.initializeCities(gameStateEntity)
        this.initializeTroops(troopCreationController, gameStateEntity)

        val playScreen = PlayScreen(game, engine, turnController, pauseController, selectionController)
        winSystem.onWin = { winner -> playScreen.goToWin(winner) }
        return playScreen
    }

    private fun setUpSystems(turnSystem: TurnSystem, selectionSystem: SelectionSystem, movementSystem: MovementSystem, collisionSystem: CollisionSystem, troopCreationSystem: TroopCreationSystem, territorySystem: TerritorySystem, troopHighlightSystem: TroopHighlightSystem, winSystem: WinSystem) {
        engine.addSystem(turnSystem)
        engine.addSystem(selectionSystem)
        engine.addSystem(movementSystem)
        engine.addSystem(collisionSystem)
        engine.addSystem(troopCreationSystem)
        engine.addSystem(territorySystem)
        engine.addSystem(troopHighlightSystem)
        engine.addSystem(winSystem)
    }

    private fun setUpWorld() {
        factory.generateRectangularGrid(18, 15)
    }

    private fun initializeCities(gameStateEntity: Entity) {
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return
        val capitalPositions = factory.generateCapitals(gs.activeTeams)

        factory.generateNormalCities(
            count = 20,
            capitalPositions = capitalPositions
        )
    }

    private fun setUpInitialGameState(): Entity {
        return factory.createGameState(listOf(TeamName.RED, TeamName.BLUE, TeamName.GREEN))
    }

    private fun initializeTroops(troopCreationController: TroopCreationController, gameState: Entity) {
        val gs = gameState[GameStateComponent.mapper] ?: return
        troopCreationController.createTroopsForTeam(gs.currentTeam)
    }
}
