package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.screens.HowToPlayScreen
import com.tdt4240.group3.view.systems.RenderSystem
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.screens.MenuScreen
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.screens.LobbyScreen
import com.tdt4240.group3.screens.OptionsScreen
import ktx.assets.disposeSafely

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine
    private lateinit var renderSystem: RenderSystem
    private lateinit var shapeRenderer: ShapeRenderer

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
        shapeRenderer = ShapeRenderer()

        engine = Engine()
        engine.addSystem(PlayerSystem())

        val playScreen = PlayScreen(this, engine)

        // Single unified render system — no TileRenderSystem, no CityRenderSystem
        renderSystem = RenderSystem(batch, shapeRenderer, playScreen.camera)
        engine.addSystem(renderSystem)

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
        renderSystem.disposeSafely()
        super.dispose()
    }
}
