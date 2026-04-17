package com.tdt4240.group3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.controller.PlayController
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.view.screens.HowToPlayScreen
import com.tdt4240.group3.view.View
import com.tdt4240.group3.network.PlayerService
import com.tdt4240.group3.screens.*
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.view.screens.MenuScreen
import com.tdt4240.group3.view.screens.OptionsScreen
import com.tdt4240.group3.view.screens.WinScreen
import ktx.assets.disposeSafely
import kotlin.math.sqrt
import java.util.UUID
import kotlinx.coroutines.launch

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine
    private lateinit var view: View
    private lateinit var shapeRenderer: ShapeRenderer

    var myPlayerId: String = ""
        private set
    var myPlayerName: String = ""
        private set

    companion object {
        const val WIDTH = 640
        const val HEIGHT = 480
        const val TITLE = "Hexa Battle"
    }

    lateinit var batch: SpriteBatch private set
    lateinit var font: BitmapFont private set



    override fun create() {
        KtxAsync.initiate()
        setupPlayerIdentity()

        batch = SpriteBatch()
        font = BitmapFont()
        shapeRenderer = ShapeRenderer()

        engine = Engine()
        engine.addSystem(PlayerSystem())

        val playController = PlayController(this, engine)
        val playScreen = playController.createScreen()
        val cols = 12f
        val rows = 11f
        val centerX = 16f * (sqrt(3.0).toFloat() * (cols / 2f) + sqrt(3.0).toFloat() / 2f * (rows / 2f))
        val centerY = 16f * (3f / 2f * (rows / 2f)) + 36f

        playScreen.camera.position.set(centerX, centerY, 0f)
        playScreen.camera.update()
        // Single unified render system — no TileRenderSystem, no CityRenderSystem
        view = View(batch, shapeRenderer, playScreen.camera, font)
        engine.addSystem(view)

        addScreen(MenuScreen(this))
        addScreen(playScreen)
        addScreen(LobbySelectScreen(this))
        addScreen(HowToPlayScreen(this))
        addScreen(OptionsScreen(this))
        addScreen(WinScreen(this))

        setScreen<MenuScreen>()
    }

    private fun setupPlayerIdentity() {
        val prefs = Gdx.app.getPreferences("HexaBattlePrefs")
        val savedId = prefs.getString("player_id", "")

        KtxAsync.launch {
            if (savedId.isEmpty()) {
                // First time — generate ID and create player in DB
                val newId = UUID.randomUUID().toString()
                val defaultName = "Guest${(1000..9999).random()}"
                val player = PlayerService.getOrCreatePlayer(newId, defaultName)
                prefs.putString("player_id", newId)
                prefs.flush()
                myPlayerId = newId
                myPlayerName = player?.displayName ?: defaultName
            } else {
                // Returning player — fetch name from DB
                val player = PlayerService.getOrCreatePlayer(savedId)
                myPlayerId = savedId
                myPlayerName = player?.displayName ?: "Guest"
            }
        }
    }

    override fun dispose() {
        font.disposeSafely()
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
        super.dispose()
    }
}
