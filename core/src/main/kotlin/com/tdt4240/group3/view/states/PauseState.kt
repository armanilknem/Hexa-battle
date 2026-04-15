package com.tdt4240.group3.view.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.tdt4240.group3.view.screens.PlayScreen
import ktx.app.clearScreen

class PauseState : PlaySubState {

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
        clearScreen(0.1f, 0.1f, 0.35f, 1f)
        screen.getFont().draw(screen.getBatch(), "Play State", 100f, 150f)
        screen.getFont().draw(screen.getBatch(), "Pause Substate", 100f, 100f)
    }
}
