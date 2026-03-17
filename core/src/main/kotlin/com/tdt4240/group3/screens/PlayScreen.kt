package com.tdt4240.group3.screens

import com.tdt4240.group3.Hexa_Battle
import ktx.app.KtxScreen
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.app.clearScreen
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle) : KtxScreen  {
    override fun render(delta: Float) {
        update(delta)

        clearScreen(0.1f, 0.35f, 0.1f, 1f)

        game.batch.use {
            // TODO draw Game
        }
    }

    private fun update(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen<MenuScreen>()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen<LobbyScreen>()
        }
    }

    override fun dispose() {
        super.dispose()
    }
}
