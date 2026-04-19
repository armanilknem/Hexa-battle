package com.tdt4240.group3.view.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.tdt4240.group3.view.screens.PlayScreen

class PauseState : PlaySubState {
    private val backgroundTexture = Texture(Gdx.files.internal("backgrounds/PauseBackground.png"))

    override fun handleInput(screen: PlayScreen) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            screen.changeState(PlayerTurnState())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            screen.changeState(EnemyTurnState())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            screen.goToMenu()
        }
    }

    override fun update(screen: PlayScreen, delta: Float) {

    }

    override fun render(screen: PlayScreen) {
        val batch = screen.getBatch()
        val previousProjection = batch.projectionMatrix.cpy()
        batch.projectionMatrix = batch.projectionMatrix.idt()
        batch.draw(backgroundTexture, -1f, -1f, 2f, 2f)
        batch.projectionMatrix = previousProjection
    }

    override fun exit(screen: PlayScreen) {
        backgroundTexture.dispose()
    }
}
