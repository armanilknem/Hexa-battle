package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.screens.MenuScreen
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.screens.LobbyScreen

class Hexa_Battle : KtxGame<KtxScreen>() {

    companion object {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val TITLE = "Hexa Battle"
    }

    lateinit var batch: SpriteBatch private set

    override fun create() {
        KtxAsync.initiate()

        batch = SpriteBatch()

        addScreen(MenuScreen(this))
        addScreen(PlayScreen(this))
        addScreen(LobbyScreen(this))

        setScreen<MenuScreen>()
    }
    override fun dispose() {
        super.dispose()
        batch.dispose()
    }
}
