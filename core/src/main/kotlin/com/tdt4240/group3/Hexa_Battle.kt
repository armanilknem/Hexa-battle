package com.tdt4240.group3

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.PlayerSystem
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use

class Hexa_Battle : KtxGame<KtxScreen>() {
    private lateinit var engine: Engine

    override fun create() {
        KtxAsync.initiate()

        // 1. Initialize the Ashley Engine
        engine = Engine()

        // 2. Add the PlayerSystem to the engine
        engine.addSystem(PlayerSystem())

        // 3. Initialize the EntityFactory
        val factory = EntityFactory(engine)

        // 4. Create a test player to verify functionality
        factory.createPlayer("Sander")

        // 5. Pass the engine to the screen so it can be updated
        addScreen(FirstScreen(engine))
        setScreen<FirstScreen>()
    }
}

class FirstScreen(private val engine: Engine) : KtxScreen {
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(Linear, Linear) }
    private val batch = SpriteBatch()

    override fun render(delta: Float) {
        // Clear the screen
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        // 6. Update the Ashley engine every frame
        // This will trigger PlayerSystem.processEntity()
        engine.update(delta)

        batch.use {
            it.draw(image, 100f, 160f)
        }
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
    }
}
