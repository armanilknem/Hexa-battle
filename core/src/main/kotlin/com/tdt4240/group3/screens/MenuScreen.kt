package com.tdt4240.group3.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.tdt4240.group3.Hexa_Battle
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use


class MenuScreen(private val game: Hexa_Battle) : KtxScreen {
    override fun render(delta: Float) {
        clearScreen(0.15f, 0.15f, 0.2f, 1f)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen<LobbyScreen>()
        }

        game.batch.use {
            game.font.draw(game.batch, "Menu State", 100f, 150f)
            // TODO draw Menu
        }
    }

    override fun dispose() {
        super.dispose()
    }

}
