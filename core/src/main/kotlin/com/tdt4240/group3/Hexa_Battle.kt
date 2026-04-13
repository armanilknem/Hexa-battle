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
import com.tdt4240.group3.screens.*
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import ktx.assets.disposeSafely
import java.util.UUID

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine
    private lateinit var cityRenderSystem: CityRenderSystem
    private lateinit var shapeRenderer: ShapeRenderer

    var myPlayerId: String = ""
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

        // 1. Initialize the Ashley Engine
        engine = Engine()

        // 2. Add the systems to the engine
        engine.addSystem(PlayerSystem())
        val playScreen = PlayScreen(this, engine)
        engine.addSystem(TileRenderSystem(shapeRenderer, playScreen.camera))
        cityRenderSystem = CityRenderSystem(batch, playScreen.camera)
        engine.addSystem(cityRenderSystem)

        // 3. Initialize the EntityFactory
        val factory = EntityFactory(engine)

        // 4. Create a test player to verify functionality
        factory.createPlayer("Sander")
        factory.generateRectangularGrid(12, 11)

        // 5. Create a test city
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

        // Start at Menu
        setScreen<MenuScreen>()
    }

    private fun setupPlayerIdentity() {
        val prefs = Gdx.app.getPreferences("HexaBattlePrefs")
        var savedId = prefs.getString("player_id", "")

        if (savedId.isEmpty()) {
            savedId = UUID.randomUUID().toString()
            prefs.putString("player_id", savedId)
            prefs.flush()
        }
        myPlayerId = savedId
    }

    override fun dispose() {
        font.disposeSafely()
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
        cityRenderSystem.disposeSafely()
        super.dispose()
    }
}
