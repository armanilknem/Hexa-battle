package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import com.tdt4240.group3.network.LobbyGameStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ktx.ashley.allOf
import ktx.ashley.get

/**
 * Drives turn progression and the inactivity/anti-AFK timeout.
 *
 * Each frame it checks whether the local player has exhausted their moves (auto-ends turn)
 * or whether a remote player has been inactive too long (strikes; eliminates after
 * [GameConstants.INACTIVITY_STRIKE_LIMIT] strikes). Calls [onTurnEnded] and pushes the
 * new turn to Supabase at the end of each turn.
 */
class TurnSystem(
    private val lobbyId: Int,
    private val myPlayerId: String
) : EntitySystem() {

    private val gameStateFamily = allOf(GameStateComponent::class).get()
    private val selectableTroopFamily = allOf(TroopComponent::class, TeamComponent::class, SelectableComponent::class).get()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var onTurnEnded: (() -> Unit)? = null

    var inactivityTimer = 0f
        private set

    private val inactivityCounts = mutableMapOf<Int, Int>()
    private var startOfTurn = true

    /**
     * Resets the inactivity timer and clears the AFK strike count for [actingPlayerId].
     * The reset is a no-op when [actingPlayerId] is not the current player — this guards
     * against stale [com.tdt4240.group3.network.MultiplayerManager] map-state events that
     * arrive after the turn has already advanced to the next player and would otherwise
     * zero-out the new player's accumulated strike count.
     */
    fun resetActivityTimer(actingPlayerId: String) {
        val gs = engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper) ?: return
        if (actingPlayerId != gs.playerOrder.getOrNull(gs.currentPlayerIndex)) return
        inactivityTimer = 0f
        inactivityCounts[gs.currentPlayerIndex] = 0
    }

    override fun update(deltaTime: Float) {
        val gameStateEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return

        if (gameStateEntity[NeedsTroopSpawnComponent.mapper] != null) return

        if (startOfTurn) {
            val selectableTroops = engine.getEntitiesFor(selectableTroopFamily)
                .filter { it[TeamComponent.mapper]?.team == gs.currentTeam }
            gs.movesLeft = minOf(selectableTroops.size, GameConstants.MAX_MOVES_PER_TURN)
            startOfTurn = false
        }

        if (gs.eliminatedTeams.contains(gs.currentTeam)) {
            endTurn()
            return
        }

        val isMyTurn = gs.playerOrder.getOrNull(gs.currentPlayerIndex) == myPlayerId
        inactivityTimer += deltaTime

        if (isMyTurn && gs.movesLeft < 1) {
            endTurn()
        } else if (!isMyTurn && inactivityTimer >= GameConstants.INACTIVITY_TIMEOUT_SECONDS) {
            val idx = gs.currentPlayerIndex
            val strikes = (inactivityCounts[idx] ?: 0) + 1
            inactivityCounts[idx] = strikes
            if (strikes >= GameConstants.INACTIVITY_STRIKE_LIMIT) {
                gs.eliminatedTeams.add(gs.currentTeam)
                inactivityCounts.remove(idx)
            }
            endTurn()
        }
    }

    fun endTurn() {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameState[GameStateComponent.mapper] ?: return

        if (gs.playerOrder.isEmpty()) return
        startOfTurn = true

        var nextIndex = gs.currentPlayerIndex + 1
        if (nextIndex >= gs.playerOrder.size) {
            nextIndex = 0
            gs.turnCount++
        }

        var steps = 0
        while (steps < gs.playerOrder.size) {
            val team = gs.activeTeams.getOrElse(nextIndex) { Team.NONE }
            if (team != Team.NONE && !gs.eliminatedTeams.contains(team)) break
            nextIndex++
            if (nextIndex >= gs.playerOrder.size) {
                nextIndex = 0
                gs.turnCount++
            }
            steps++
        }
        if (steps >= gs.playerOrder.size) return // all players eliminated — WinSystem handles this

        gs.currentPlayerIndex = nextIndex
        inactivityTimer = 0f

        requestTroopSpawn(gameState)
        onTurnEnded?.invoke()

        scope.launch {
            LobbyGameStateService.updateTurn(
                lobbyId      = lobbyId,
                nextPlayerId = gs.playerOrder[gs.currentPlayerIndex],
                turnNumber   = gs.turnCount
            )
        }
    }

    fun onRemoteTurnStarted() {
        startOfTurn = true
        inactivityTimer = 0f
    }

    fun isCurrentTeam(team: Team): Boolean {
        val gs = engine.getEntitiesFor(gameStateFamily).firstOrNull()
            ?.get(GameStateComponent.mapper) ?: return false
        return gs.currentTeam == team
    }

    private fun requestTroopSpawn(gameStateEntity: Entity) {
        if (gameStateEntity[NeedsTroopSpawnComponent.mapper] == null) {
            gameStateEntity.add(NeedsTroopSpawnComponent())
        }
    }
}
