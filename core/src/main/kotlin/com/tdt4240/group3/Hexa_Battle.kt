package com.tdt4240.group3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.model.systems.TileRenderSystem
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.CityRenderSystem
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.network.PlayerService
import com.tdt4240.group3.screens.*
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.assets.disposeSafely
import java.util.UUID
import kotlinx.coroutines.launch

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine
    private lateinit var cityRenderSystem: CityRenderSystem
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
        val playScreen = PlayScreen(this, engine)
        engine.addSystem(TileRenderSystem(shapeRenderer, playScreen.camera))
        cityRenderSystem = CityRenderSystem(batch, playScreen.camera)
        engine.addSystem(cityRenderSystem)

        val factory = EntityFactory(engine)
        factory.createPlayer("Sander")
        factory.generateRectangularGrid(12, 11)
        factory.createCity(
            name = "Manchester",
            isCapital = true,
            baseProduction = 20,
            q = 5,
            r = 5,
            team = TeamComponent.TeamName.RED
        )

        addScreen(MenuScreen(this))
        addScreen(playScreen)
        addScreen(LobbySelectScreen(this))
        addScreen(HowToPlayScreen(this))
        addScreen(OptionsScreen(this))

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
        cityRenderSystem.disposeSafely()
        super.dispose()
    }
}
