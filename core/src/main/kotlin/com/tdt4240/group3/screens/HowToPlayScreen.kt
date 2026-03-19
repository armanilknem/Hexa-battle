package com.tdt4240.group3.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.tdt4240.group3.Hexa_Battle
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class HowToPlayScreen(private val game: Hexa_Battle) : KtxScreen {
    override fun render(delta: Float) {
        clearScreen(0.2f, 0.2f, 0.15f, 1f)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen<MenuScreen>()
        }
        game.batch.use {
            // TODO draw Lobby
            game.font.draw(it, "How To play", 100f, 150f)
        }
    }

    override fun dispose() {
        super.dispose()
    }
}
