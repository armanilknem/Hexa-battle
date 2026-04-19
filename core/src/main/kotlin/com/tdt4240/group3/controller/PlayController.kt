package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.ecs.components.GameStateComponent
import com.tdt4240.group3.model.ecs.entities.EntityFactory
import com.tdt4240.group3.model.ecs.systems.*
import com.tdt4240.group3.model.team.TeamName
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.SupabaseClient
import com.tdt4240.group3.network.model.LobbyMapState
import com.tdt4240.group3.view.screens.PlayScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktx.ashley.get

class PlayController(
    private val game: Hexa_Battle,
    private val engine: Engine,
) {
    private val factory: EntityFactory = EntityFactory(engine)
    private val scope = CoroutineScope(Dispatchers.Default)

    fun createScreen(lobbyId: Int, myPlayerId: String, playerOrder: List<String>): PlayScreen {
        val turnSystem = TurnSystem(lobbyId)
        val selectionSystem = SelectionSystem()
        val movementSystem = MovementSystem()
        val collisionSystem = CollisionSystem()
        val troopCreationSystem = TroopCreationSystem(engine)
        val territorySystem = TerritorySystem(lobbyId)
        val troopHighlightSystem = TroopHighlightSystem(turnSystem)
        val winSystem = WinSystem()

        this.setUpSystems(turnSystem, selectionSystem, movementSystem, collisionSystem, troopCreationSystem, territorySystem, troopHighlightSystem, winSystem)

        val turnController = TurnController(turnSystem = turnSystem)
        val troopCreationController = TroopCreationController(troopCreationSystem)
        val pauseController = PauseController()
        val selectionController = SelectionController(engine)

        val gameStateEntity = setUpInitialGameState(playerOrder.size)
        val gs = gameStateEntity[GameStateComponent.mapper]!!

        gs.playerOrder = playerOrder
        gs.currentPlayerIndex = playerOrder.indexOf(myPlayerId)

        setUpWorld()
        initializeCities(gameStateEntity, lobbyId, myPlayerId == playerOrder.first())
        initializeTroops(troopCreationController, gameStateEntity)

        val playScreen = PlayScreen(
            game,
            engine,
            turnController,
            pauseController,
            selectionController,
            lobbyId,
            myPlayerId
        )

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

    private fun initializeCities(gameStateEntity: Entity, lobbyId: Int, isHost: Boolean) {
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return
        val capitalPositions = factory.generateCapitals(gs.activeTeams)

        val lobbyMapStates = capitalPositions.mapIndexed { index, capitalPosition ->
            LobbyMapState(
                lobbyId = lobbyId,
                q = capitalPosition.first,
                r = capitalPosition.second,
                ownerId = gs.playerOrder[index],
                strength = 0
            )
        }
        scope.launch {
            LobbyGameStateService.setLobbyMapStates(lobbyMapStates)
        }

        factory.generateNormalCities(
            count = 20,
            capitalPositions = capitalPositions
        )
    }

    private fun setUpInitialGameState(playerCount: Int): Entity {
        val teamNames = when (playerCount) {
            1 -> listOf(TeamName.RED)
            2 -> listOf(TeamName.RED, TeamName.BLUE)
            3 -> listOf(TeamName.RED, TeamName.BLUE, TeamName.GREEN)
            4 -> listOf(TeamName.RED, TeamName.BLUE, TeamName.PURPLE, TeamName.GREEN)
            else -> listOf(TeamName.RED, TeamName.BLUE, TeamName.PURPLE, TeamName.GREEN)
        }

        return factory.createGameState(teamNames)
    }

    private fun initializeTroops(troopCreationController: TroopCreationController, gameState: Entity) {
        val gs = gameState[GameStateComponent.mapper] ?: return
        troopCreationController.createTroopsForTeam(gs.currentTeam)
    }
}
