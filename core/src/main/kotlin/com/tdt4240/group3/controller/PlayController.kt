package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.MapGenerator
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.marker.NeedsTroopSpawnComponent
import com.tdt4240.group3.model.entities.GameStateConfig
import com.tdt4240.group3.model.entities.GameStateFactory
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.model.systems.*
import com.tdt4240.group3.network.LobbyGameStateService
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.network.model.LobbyMapState
import com.tdt4240.group3.view.screens.PlayScreen
import com.tdt4240.group3.view.styleRegistries.TeamVisualRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ktx.ashley.get

class PlayController(
    private val game: Hexa_Battle,
    private val engine: Engine,
) {
    private val mapGenerator = MapGenerator(engine)
    private val troopFactory = TroopFactory(engine)
    private val gameStateFactory = GameStateFactory(engine)
    private val scope = CoroutineScope(Dispatchers.Default)

    fun createScreen(lobbyId: Int, myPlayerId: String, playerOrder: List<String>): PlayScreen {
        val turnSystem = TurnSystem(lobbyId)
        val selectionSystem = SelectionSystem()
        val movementSystem = MovementSystem()
        val collisionSystem = CollisionSystem()
        val troopCreationSystem = TroopCreationSystem(engine)
        val territorySystem = TerritorySystem(lobbyId)
        val winSystem = WinSystem()

        val turnController = TurnController(turnSystem = turnSystem)
        val pauseController = PauseController()
        val selectionController = SelectionController(engine)

        val gameStateEntity = setUpInitialGameState(playerOrder.size)
        val gs = gameStateEntity[GameStateComponent.mapper]!!

        val myIndex = playerOrder.indexOf(myPlayerId)
        val myTeam = gs.activeTeams.getOrElse(myIndex) { com.tdt4240.group3.model.Team.NONE }
        game.myTeam = myTeam
        val troopHighlightSystem = TroopHighlightSystem(turnSystem, myTeam)

        setUpSystems(
            turnSystem, selectionSystem, movementSystem, collisionSystem,
            troopCreationSystem, territorySystem, troopHighlightSystem, winSystem
        )

        gs.playerOrder = playerOrder
        gs.currentPlayerIndex = 0

        setUpWorld()

        val isHost = myPlayerId == playerOrder.first()
        val capitalPositions = mapGenerator.generateCapitals(gs.activeTeams, randomSeed = lobbyId.hashCode())
        mapGenerator.generateCities(count = 20, capitalPositions = capitalPositions, randomSeed = lobbyId.hashCode())

        if (isHost) {
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
        }

        gameStateEntity.add(engine.createComponent(NeedsTroopSpawnComponent::class.java))

        val playScreen = PlayScreen(
            game,
            engine,
            turnController,
            pauseController,
            selectionController,
            lobbyId,
            myPlayerId,
            troopFactory
        )

        winSystem.onWin = { winner ->
            val winnerId = gs.playerOrder[gs.activeTeams.indexOf(winner)]
            if (myPlayerId === winnerId) {
                scope.launch {
                    LobbyService.endGame(lobbyId, winnerId)
                }
            }
            playScreen.goToWin(winner)
        }
        turnSystem.onTurnEnded = { playScreen.onTurnChanged(false) }
        return playScreen
    }

    private fun setUpSystems(
        turnSystem: TurnSystem,
        selectionSystem: SelectionSystem,
        movementSystem: MovementSystem,
        collisionSystem: CollisionSystem,
        troopCreationSystem: TroopCreationSystem,
        territorySystem: TerritorySystem,
        troopHighlightSystem: TroopHighlightSystem,
        winSystem: WinSystem
    ) {
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
        MapGenerator(engine).generateRectangularGrid(18, 15)
    }

    private fun setUpInitialGameState(playerCount: Int): Entity {
        val teamNames = TeamVisualRegistry.visuals.keys.take(playerCount)
        return gameStateFactory.createEntity(GameStateConfig(teamNames))
    }
}
