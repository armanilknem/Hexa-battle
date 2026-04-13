package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.screens.HowToPlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.screens.MenuScreen
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.screens.LobbyScreen
import com.tdt4240.group3.screens.OptionsScreen
import ktx.assets.disposeSafely
import kotlin.math.sqrt

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine

    companion object {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val TITLE = "Hexa Battle"
    }

    lateinit var batch: SpriteBatch private set
    lateinit var font: BitmapFont private set
    lateinit var shapeRenderer: ShapeRenderer private set



    override fun create() {
        KtxAsync.initiate()

        batch = SpriteBatch()
        font = BitmapFont()
        shapeRenderer = ShapeRenderer()

        engine = Engine()
        engine.addSystem(PlayerSystem())

        val playScreen = PlayScreen(this, engine)
        // Center camera on grid
        val cols = 12f
        val rows = 11f
        val centerX = 16f * (sqrt(3.0).toFloat() * (cols / 2f) + sqrt(3.0).toFloat() / 2f * (rows / 2f))
        val centerY = 16f * (3f / 2f * (rows / 2f)) + 36f

        playScreen.camera.position.set(centerX, centerY, 0f)
        playScreen.camera.update()
        // Single unified render system — no TileRenderSystem, no CityRenderSystem


        addScreen(MenuScreen(this))
        addScreen(playScreen)
        addScreen(LobbyScreen(this))
        addScreen(HowToPlayScreen(this))
        addScreen(OptionsScreen(this))
        setScreen<MenuScreen>()
    }
    override fun dispose() {
        font.disposeSafely()
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
        super.dispose()
    }
}
