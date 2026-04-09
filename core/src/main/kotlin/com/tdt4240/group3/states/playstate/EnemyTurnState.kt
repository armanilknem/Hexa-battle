package com.tdt4240.group3.states.playstate

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.screens.PlayScreen

class EnemyTurnState : PlaySubState {
    override val backgroundColor = Triple(0.35f, 0.1f, 0.1f)  // red

    override fun handleInput(screen: PlayScreen) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            screen.changeState(PauseState())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            endTurn(screen)
        }
    }

    override fun update(screen: PlayScreen, delta: Float) {

    }

    override fun render(screen: PlayScreen) {
//        clearScreen(0.35f, 0.1f, 0.1f, 1f)
        screen.getFont().draw(screen.getBatch(), "Play State", 100f, 150f)
        screen.getFont().draw(screen.getBatch(), "Enemy Turn Substate", 100f, 100f)
    }

    fun endTurn(screen: PlayScreen) {
        screen.changeState(PlayerTurnState())
    }

}
