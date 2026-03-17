package com.tdt4240.group3.states.playstate

import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.screens.PlayScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.app.clearScreen

class PlayerTurnState : PlaySubState {
    override fun handleInput(screen: PlayScreen) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            screen.changeState(PauseState())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            screen.changeState(EnemyTurnState())
        }
    }

    override fun update(screen: PlayScreen, delta: Float) {

    }

    override fun render(screen: PlayScreen) {
        clearScreen(0.1f, 0.35f, 0.1f, 1f)
        screen.getFont().draw(screen.getBatch(), "Play State", 100f, 150f)
        screen.getFont().draw(screen.getBatch(), "Player Turn Substate", 100f, 100f)
    }
}
