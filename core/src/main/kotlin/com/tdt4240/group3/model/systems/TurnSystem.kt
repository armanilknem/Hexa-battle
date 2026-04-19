package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import com.tdt4240.group3.network.LobbyGameStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.tdt4240.group3.model.Team
import ktx.ashley.allOf
import ktx.ashley.get

class TurnSystem(private val lobbyId: Int) : EntitySystem() {
    private val gameStateFamily = allOf(GameStateComponent::class).get()
    private val scope = CoroutineScope(Dispatchers.Default)
    var onTurnEnded: (() -> Unit)? = null

    override fun update(deltaTime: Float) {
        val gameStateEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return

        if (gameStateEntity.getComponent(NeedsTroopSpawnComponent::class.java) != null) {
            return
        }

        val selectableTroops = engine.getEntitiesFor(allOf(SelectableComponent::class).get()).toList()

        if (selectableTroops.isEmpty() || gs.movesLeft < 1) {
            endTurn()
        }
    }

    fun endTurn() {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameState[GameStateComponent.mapper] ?: return

        if (gs.playerOrder.isEmpty()) return

        gs.currentPlayerIndex = (gs.currentPlayerIndex + 1) % gs.playerOrder.size

        if (gs.currentPlayerIndex == 0) {
            gs.turnCount++
        }

        gs.movesLeft = 5
        requestTroopSpawn(gameState)
        onTurnEnded?.invoke()

        scope.launch {
            LobbyGameStateService.updateTurn(
                lobbyId = lobbyId,
                nextPlayerId = gs.playerOrder[gs.currentPlayerIndex],
                turnNumber = gs.turnCount
            )
        }
    }

    fun isCurrentTeam(team: Team): Boolean {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return false
        val gs = gameState[GameStateComponent.mapper] ?: return false
        return gs.currentTeam == team
    }

    private fun requestTroopSpawn(gameStateEntity: Entity) {
        if (gameStateEntity.getComponent(NeedsTroopSpawnComponent::class.java) == null) {
            gameStateEntity.add(engine.createComponent(NeedsTroopSpawnComponent::class.java))
        }
    }
}
