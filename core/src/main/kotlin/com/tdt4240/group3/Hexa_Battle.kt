package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.model.systems.CityRenderSystem
import com.tdt4240.group3.model.components.TeamComponent
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync
import com.tdt4240.group3.screens.MenuScreen
import com.tdt4240.group3.screens.PlayScreen
import com.tdt4240.group3.screens.LobbyScreen
import ktx.assets.disposeSafely

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine
    private lateinit var cityRenderSystem: CityRenderSystem


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

        // 1. Initialize the Ashley Engine
        engine = Engine()

        // 2. Add the systems to the engine
        engine.addSystem(PlayerSystem())
        cityRenderSystem = CityRenderSystem(batch)
        engine.addSystem(cityRenderSystem)

        // 3. Initialize the EntityFactory
        val factory = EntityFactory(engine)

        // 4. Create a test player to verify functionality
        factory.createPlayer("Sander")

        // 5. Create a test city
        factory.createCity(
            name = "Manchester",
            isCapital = true,
            baseProduction = 20,
            x = 100,
            y = 100,
            team = TeamComponent.TeamName.P1
        )

        addScreen(MenuScreen(this))
        addScreen(PlayScreen(this, engine))
        addScreen(LobbyScreen(this))
        setScreen<MenuScreen>()
    }
    override fun dispose() {
        super.dispose()
        font.disposeSafely()
        batch.dispose()
        cityRenderSystem.disposeSafely()
    }
}
