package com.tdt4240.group3.screens

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.states.playstate.PlayerTurnState
import ktx.app.KtxScreen
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine) : KtxScreen  {

    private var currentState : PlaySubState = PlayerTurnState()
    private var previousState : PlaySubState = PlayerTurnState()


    init {
        currentState.enter(this)
    }

    override fun render(delta: Float) {
        currentState.handleInput(this)
        currentState.update(this, delta)

        game.batch.use {
            currentState.render(this@PlayScreen)
            // 6. Update the Ashley engine every frame
            // This will trigger PlayerSystem.processEntity()
            engine.update(delta)
        }
    }

    fun changeState(newState : PlaySubState) {
        currentState.exit(this)
        previousState = currentState
        currentState = newState
        currentState.enter(this)
    }

    fun goToMenu() {
        game.setScreen<MenuScreen>()
    }

//    fun goToLobby() {
//        game.setScreen<LobbyScreen>()
//    }

    fun getBatch() = game.batch
    fun getFont() = game.font
//    fun getPreviousState() = previousState
//    fun getCurrentState() = currentState

    override fun dispose() {
        super.dispose()
    }
}
