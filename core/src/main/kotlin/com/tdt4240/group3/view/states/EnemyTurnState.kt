package com.tdt4240.group3.view.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.view.screens.PlayScreen

class EnemyTurnState : PlaySubState {
    override val backgroundColor = Triple(0.35f, 0.1f, 0.1f)  // red

    override fun handleInput(screen: PlayScreen) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            screen.changeState(PauseState())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            screen.changeState(PlayerTurnState())
        }
    }

    override fun update(screen: PlayScreen, delta: Float) {

    }

    override fun render(screen: PlayScreen) {
//        clearScreen(0.35f, 0.1f, 0.1f, 1f)
    }

}
