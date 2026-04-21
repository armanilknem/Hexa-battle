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
import com.kotcrab.vis.ui.widget.VisTextField
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.network.PlayerService
import com.tdt4240.group3.view.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class OptionsScreen(private val game: Hexa_Battle) : KtxScreen {

    private var backgroundTexture: Texture? = null
    private lateinit var stage: Stage
    private lateinit var statusLabel: VisLabel

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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

        val usernameLabel = VisLabel("USERNAME").apply {
            setFontScale(1.2f)
        }

        val usernameField = VisTextField(game.myPlayerName).apply {
            maxLength = 20
        }

        statusLabel = VisLabel("").apply {
            setFontScale(1.1f)
            color = Color.BLACK
        }

        val saveBtn = VisTextButton("SAVE").apply {
            color = Color.BLACK
            onClick {
                val newName = usernameField.text.trim()
                if (newName.isEmpty()) {
                    showStatus("Name cannot be empty.", Color.RED)
                    return@onClick
                }
                if (newName == game.myPlayerName) {
                    showStatus("No changes to save.", Color.BLACK)
                    return@onClick
                }
                showStatus("Saving...", Color.WHITE)
                scope.launch {
                    val success = PlayerService.updateDisplayName(game.myPlayerId, newName)
                    Gdx.app.postRunnable {
                        if (success) {
                            game.setPlayerName(newName)
                            showStatus("Saved!", Color.valueOf("#006600"))
                        } else {
                            showStatus("Failed to save. Try again.", Color.RED)
                        }
                    }
                }
            }
        }

        val backBtn = VisTextButton("BACK").apply {
            onClick { game.setScreen<MenuScreen>() }
            color = Color.BLACK
        }

        root.add(titleLabel).padBottom(32f).row()
        root.add(usernameLabel).left().width(280f).padBottom(6f).row()
        root.add(usernameField).width(280f).height(44f).padBottom(8f).row()
        root.add(saveBtn).width(280f).height(44f).padBottom(8f).row()
        root.add(statusLabel).width(280f).padBottom(24f).row()
        root.add(backBtn).width(280f).height(52f)

        stage.addActor(root)
    }

    private fun showStatus(message: String, color: Color) {
        statusLabel.setText(message)
        statusLabel.color = color
    }

    override fun render(delta: Float) {
        with(ViewConfig.BG_DARK_OLIVE) { clearScreen(r, g, b, a) }
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
        scope.cancel()
    }
}
