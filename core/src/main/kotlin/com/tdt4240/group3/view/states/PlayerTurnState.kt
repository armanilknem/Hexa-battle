package com.tdt4240.group3.view.states

import com.tdt4240.group3.view.screens.PlayScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

class PlayerTurnState : PlaySubState {
    override val backgroundColor = Triple(0.1f, 0.35f, 0.1f)  // green

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
//        clearScreen(0.1f, 0.35f, 0.1f, 1f)
       }
}
