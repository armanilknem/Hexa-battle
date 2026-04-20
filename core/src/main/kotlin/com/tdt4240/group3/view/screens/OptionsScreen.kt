package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.view.ViewConfig
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class OptionsScreen(private val game: Hexa_Battle) : KtxScreen {

    private var backgroundTexture: Texture? = null
    private lateinit var stage: Stage

    override fun show() {
        backgroundTexture = Texture(Gdx.files.internal("backgrounds/OptionsBackground.png"))
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
        Gdx.input.inputProcessor = stage

        val root = Table().apply {
            setFillParent(true)
            center()
            background = TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val titleLabel = VisLabel("OPTIONS").apply {
            setFontScale(2f)
        }
        // TODO: Add actual options here (username, sound, music, etc.)
        val backBtn = VisTextButton("BACK").apply {
            onClick { game.setScreen<MenuScreen>() }
            color = Color.BLACK
        }

        root.add(titleLabel).padBottom(48f).row()
        root.add(backBtn).width(280f).height(52f)

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        clearScreen(0.2f, 0.2f, 0.15f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        stage.dispose()
        backgroundTexture?.dispose()
    }
}
