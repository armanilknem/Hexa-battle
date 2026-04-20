package com.tdt4240.group3.model.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.tdt4240.group3.model.MapGenerator
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.UnitType
import ktx.ashley.allOf
import ktx.ashley.get

class TroopCreationSystem(private val engine: Engine) : EntitySystem() {
    private val mapGenerator = MapGenerator(engine)
    private val cityFamily = allOf(CityComponent::class, PositionComponent::class, TeamComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class, NeedsTroopSpawnComponent::class).get()
    private val troopFamily = allOf(
        TroopComponent::class,
        CombatComponent::class,
        PositionComponent::class,
        TeamComponent::class
    ).get()

    // Prevents double-reinforcement when NeedsTroopSpawnComponent fires more than once
    // for the same (turnCount, playerIndex) state (e.g. from both endTurn and MultiplayerManager).
    private var lastSpawnedTurn: Int = -1
    private var lastSpawnedPlayerIndex: Int = -1

    override fun update(deltaTime: Float) {
        val gameStateEntity = engine.getEntitiesFor(gameStateFamily).firstOrNull() ?: return
        val gs = gameStateEntity[GameStateComponent.mapper] ?: return

        val alreadySpawnedThisState = gs.turnCount == lastSpawnedTurn && gs.currentPlayerIndex == lastSpawnedPlayerIndex
        if (!alreadySpawnedThisState) {
            if (gs.turnCount == 0) {
                // Game start: give every team one troop, no production bonus yet
                gs.activeTeams.forEach { team -> createTroopsForTeam(team) }
                gs.turnCount = 1
            } else if (gs.turnCount > 1) {
                // Normal turns: reinforce only the current team
                createTroopsForTeam(gs.currentTeam)
            }
            // turn 1, non-initial: skip — troops already created at game start
            lastSpawnedTurn = gs.turnCount
            lastSpawnedPlayerIndex = gs.currentPlayerIndex
        }

        markSelectable(gs)
        gameStateEntity.remove(NeedsTroopSpawnComponent::class.java)
    }

    fun createTroopFromCity(cityEntity: Entity) {
        val cityComp = cityEntity[CityComponent.mapper] ?: return
        val cityPos = cityEntity[PositionComponent.mapper] ?: return
        val cityTeam = cityEntity[TeamComponent.mapper]?.team ?: return
        val existingTroop = engine.getEntitiesFor(troopFamily).find {
            val p = it[PositionComponent.mapper]
            p?.q == cityPos.q && p.r == cityPos.r
        }
        if (existingTroop != null) {
            val troopComp = existingTroop[TroopComponent.mapper]!!
            val combatComp = existingTroop[CombatComponent.mapper] ?: return
            val troopTeam = existingTroop[TeamComponent.mapper]?.team ?: return
            if (troopTeam == cityTeam) {
                val total = troopComp.strength + cityComp.baseProduction
                if (total <= combatComp.maxStackSize) {
                    troopComp.strength = total
                }
                else {
                    troopComp.strength = combatComp.maxStackSize
                }
            }
        } else {
            // generate baseTroops from cities
            val newTroop = mapGenerator.createTroopFromCity(cityEntity, UnitType.SOLDIER)
            newTroop.add(engine.createComponent(SelectableComponent::class.java))
        }
    }

    fun createTroopsForTeam(team: Team) {
        engine.getEntitiesFor(cityFamily)
            .filter { it[TeamComponent.mapper]?.team == team }
            .forEach {
                createTroopFromCity(it)
                it.add(engine.createComponent(TerritoryComponent::class.java))
            }
    }

    fun markSelectable(gs: GameStateComponent) {
        engine.getEntitiesFor(troopFamily).forEach { troop ->
            troop.remove(SelectableComponent::class.java)
        }

        engine.getEntitiesFor(troopFamily)
            .filter { it[TeamComponent.mapper]?.team == gs.currentTeam }
            .forEach { troop ->
                troop.add(engine.createComponent(SelectableComponent::class.java))
            }
    }

}
