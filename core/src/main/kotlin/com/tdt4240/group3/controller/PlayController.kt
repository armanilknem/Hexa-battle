package com.tdt4240.group3.controller

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.model.MapGenerator
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get

class PlayController(
    private val game: Hexa_Battle,
    private val engine: Engine,
) {
    private val mapGenerator     = MapGenerator(engine)
    private val troopFactory     = TroopFactory(engine)
    private val gameStateFactory = GameStateFactory(engine)
    private val scope            = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val teamFamily = allOf(TeamComponent::class).get()

    fun createScreen(lobbyId: Int, myPlayerId: String, playerOrder: List<String>): PlayScreen {

        // Systems
        val turnSystem          = TurnSystem(lobbyId, myPlayerId)
        val selectionSystem     = SelectionSystem()
        val movementSystem      = MovementSystem()
        val collisionSystem     = CollisionSystem()
        val unitPromotionSystem = UnitPromotionSystem()
        val troopCreationSystem = TroopCreationSystem(engine, troopFactory)
        val territorySystem     = TerritorySystem(lobbyId)
        val winSystem           = WinSystem()

        // Game state (needed before TroopHighlightSystem to resolve myTeam)
        val gameStateEntity = setUpInitialGameState(playerOrder.size)
        val gs = gameStateEntity[GameStateComponent.mapper]!!

        val myIndex = playerOrder.indexOf(myPlayerId)
        val myTeam  = gs.activeTeams.getOrElse(myIndex) { Team.NONE }
        game.myTeam = myTeam

        val troopHighlightSystem = TroopHighlightSystem(myTeam)

        listOf(
            turnSystem, selectionSystem, movementSystem, collisionSystem,
            unitPromotionSystem, troopCreationSystem, territorySystem, troopHighlightSystem, winSystem
        ).forEach(engine::addSystem)

        gs.playerOrder        = playerOrder
        gs.currentPlayerIndex = 0

        // World generation
        setUpWorld()
        val capitalPositions = mapGenerator.generateCapitals(gs.activeTeams, randomSeed = lobbyId.hashCode())
        engine.getEntitiesFor(allOf(CapitalComponent::class).get()).forEach { capitalEntity ->
            val team = capitalEntity[TeamComponent.mapper]?.team ?: Team.NONE
            capitalEntity[CapitalComponent.mapper]?.originalTeam = team
        }
        mapGenerator.generateCities(
            count            = GameConstants.CITY_COUNT,
            capitalPositions = capitalPositions,
            randomSeed       = lobbyId.hashCode()
        )

        if (myPlayerId == playerOrder.first()) {
            scope.launch {
                LobbyGameStateService.setLobbyMapStates(
                    capitalPositions.mapIndexed { index, (q, r) ->
                        LobbyMapState(lobbyId = lobbyId, q = q, r = r,
                            ownerId = gs.playerOrder[index], strength = 0)
                    }
                )
            }
        }

        gameStateEntity.add(NeedsTroopSpawnComponent())

        // Screen + callbacks
        val playScreen = PlayScreen(
            game, engine,
            TurnController(turnSystem), PauseController, SelectionController(engine),
            lobbyId, myPlayerId, troopFactory
        )

        winSystem.onWin = { winner ->
            scope.launch { LobbyService.endGame(lobbyId, gs.playerOrder[gs.activeTeams.indexOf(winner)]) }
            playScreen.goToWin(winner)
        }
        winSystem.onPlayerEliminated = { eliminatedTeam -> handleElimination(eliminatedTeam, playScreen) }
        turnSystem.onTurnEnded = { playScreen.onTurnChanged(false) }

        return playScreen
    }

    private fun handleElimination(eliminatedTeam: Team, playScreen: PlayScreen) {
        // Remove all troops belonging to the eliminated team
        engine.getEntitiesFor(allOf(TroopComponent::class, TeamComponent::class).get())
            .toList()
            .filter { it[TeamComponent.mapper]?.team == eliminatedTeam }
            .forEach(engine::removeEntity)

        // Transfer the eliminated team's tiles and cities to whoever captured their capital
        val conquerorTeam = engine
            .getEntitiesFor(allOf(CapitalComponent::class, TeamComponent::class).get())
            .toList()
            .firstOrNull { it[CapitalComponent.mapper]?.originalTeam == eliminatedTeam }
            ?.get(TeamComponent.mapper)?.team ?: Team.NONE

        if (conquerorTeam != Team.NONE) {
            engine.getEntitiesFor(teamFamily).toList().forEach { entity ->
                val teamComp = entity[TeamComponent.mapper]!!
                if (teamComp.team == eliminatedTeam) teamComp.team = conquerorTeam
            }
        }

        if (eliminatedTeam == game.myTeam) playScreen.goToEliminated()
    }

    private fun setUpWorld() {
        mapGenerator.generateRectangularGrid(GameConstants.MAP_WIDTH, GameConstants.MAP_HEIGHT)
    }

    private fun setUpInitialGameState(playerCount: Int): Entity {
        val teamNames = TeamVisualRegistry.visuals.keys.take(playerCount)
        return gameStateFactory.createEntity(GameStateConfig(teamNames))
    }
}
