package com.tdt4240.group3.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.states.playstate.PlayerTurnState
import ktx.app.KtxScreen
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine) : KtxScreen  {

    private var currentState : PlaySubState = PlayerTurnState()
    private var previousState : PlaySubState = PlayerTurnState()
    val camera = OrthographicCamera()


    init {
        camera.setToOrtho(false, Hexa_Battle.WIDTH.toFloat(), Hexa_Battle.HEIGHT.toFloat())
        camera.position.set(
            Hexa_Battle.WIDTH / 2f,
            Hexa_Battle.HEIGHT / 2f,
            0f
        )
        currentState.enter(this)
    }

    override fun render(delta: Float) {
        val (r, g, b) = currentState.backgroundColor
        Gdx.gl.glClearColor(r, g, b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        currentState.handleInput(this)
        currentState.update(this, delta)
        // 6. Update the Ashley engine every frame
        // This will trigger PlayerSystem.processEntity()

        game.batch.projectionMatrix = camera.combined
        game.batch.use {
            currentState.render(this@PlayScreen)
        }
        engine.update(delta)
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
