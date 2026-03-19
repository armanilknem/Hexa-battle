package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.tdt4240.group3.screens.HowToPlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.screens.MenuScreen
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.screens.LobbyScreen
import com.tdt4240.group3.screens.OptionsScreen
import ktx.assets.disposeSafely

class Hexa_Battle : KtxGame<KtxScreen>() {

    companion object {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val TITLE = "Hexa Battle"
    }

    lateinit var batch: SpriteBatch private set
    lateinit var font: BitmapFont private set


    override fun create() {
        KtxAsync.initiate()

        batch = SpriteBatch()
        font = BitmapFont()

        addScreen(MenuScreen(this))
        addScreen(PlayScreen(this))
        addScreen(LobbyScreen(this))
        addScreen(HowToPlayScreen(this))
        addScreen(OptionsScreen(this))

        setScreen<MenuScreen>()
    }
    override fun dispose() {
        super.dispose()
        font.disposeSafely()
        batch.dispose()
    }
}
