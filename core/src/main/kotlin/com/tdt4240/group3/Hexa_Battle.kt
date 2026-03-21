package com.tdt4240.group3

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.tdt4240.group3.model.systems.PlayerSystem
import com.tdt4240.group3.model.systems.TileRenderSystem
import com.tdt4240.group3.model.entities.EntityFactory
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

        // 1. Initialize the Ashley Engine
        engine = Engine()

        // 2. Initialize the EntityFactory
        val factory = EntityFactory(engine)

        // 3. Create screens first to get camera correct
        val playScreen = PlayScreen(this, engine)
        addScreen(MenuScreen(this))
        addScreen(playScreen)
        addScreen(LobbyScreen(this))

        // 4. Add the systems in draw order
        engine.addSystem(PlayerSystem())
        engine.addSystem(TileRenderSystem(shapeRenderer, playScreen.camera))
        cityRenderSystem = CityRenderSystem(batch, playScreen.camera)
        engine.addSystem(cityRenderSystem)


        // 5. Create a test player to verify functionality
        factory.createPlayer("Sander")

        // 6. Generate grid
        factory.generateRectangularGrid(12, 11)

        // 7. Pass the engine to the screen so it can be updated
//        addScreen(FirstScreen(engine))
//        setScreen<FirstScreen>()

        // 8. Create a test city
        factory.createCity(
            name = "Manchester",
            isCapital = true,
            baseProduction = 20,
            q = 5,
            r = 5,
            team = TeamComponent.TeamName.P1
        )

        // 9. Start the game
        setScreen<MenuScreen>()
    }
    override fun dispose() {
        font.disposeSafely()
        batch.disposeSafely()
        shapeRenderer.disposeSafely()
        cityRenderSystem.disposeSafely()
        super.dispose()
    }
}
