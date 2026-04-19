package com.tdt4240.group3.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisTextField
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.network.model.LobbyResult
import com.tdt4240.group3.view.ViewConfig
import com.tdt4240.group3.view.screens.LobbyScreen
import com.tdt4240.group3.view.screens.MenuScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class LobbySelectScreen(private val game: Hexa_Battle) : KtxScreen {
    private val stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var backgroundTexture: Texture? = null
    private lateinit var statusLabel: VisLabel

    override fun show() {
        backgroundTexture = Texture(Gdx.files.internal("backgrounds/MenuBackground.png"))
        if (!VisUI.isLoaded()) VisUI.load()

        statusLabel = VisLabel("").apply { color = Color.WHITE }

        Gdx.input.inputProcessor = stage
        stage.clear()
        val root = Table().apply { setFillParent(true); center(); background =
            TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val title = VisLabel("MULTIPLAYER").apply { color = Color.BLACK }
        val createBtn = VisTextButton("CREATE LOBBY").apply { color = Color.BLACK }
        val codeField = VisTextField("").apply { messageText = "ENTER CODE..."; color = Color.BLACK }
        val joinBtn = VisTextButton("JOIN").apply { color = Color.BLACK }
        val backBtn = VisTextButton("BACK").apply { color = Color.BLACK }

        createBtn.onClick {
            statusLabel.setText("Creating...")
            scope.launch { handleResult(LobbyService.getOrCreateLobby(game.myPlayerId)) }
        }

        joinBtn.onClick {
            val code = codeField.text
            if (code.length == 6) {
                statusLabel.setText("Joining...")
                scope.launch { handleResult(LobbyService.joinLobbyByCode(code, game.myPlayerId)) }
            } else {
                statusLabel.setText("Invalid Code Format")
            }
        }

        backBtn.onClick { game.setScreen<MenuScreen>() }

        root.add(title).padBottom(40f).row()
        root.add(createBtn).width(280f).height(50f).padBottom(30f).row()

        val joinTable = Table()
        joinTable.add(codeField).width(180f).height(50f).padRight(10f)
        joinTable.add(joinBtn).width(90f).height(50f)

        root.add(joinTable).padBottom(20f).row()
        root.add(statusLabel).padBottom(20f).row()
        root.add(backBtn).width(280f).height(50f)

        stage.addActor(root)
    }

    private fun handleResult(result: LobbyResult) {
        when (result) {
            is LobbyResult.Success -> {
                Gdx.app.postRunnable {
                    val nextScreen = LobbyScreen(game, result.lobby)
                    if (game.containsScreen<LobbyScreen>()) {
                        game.removeScreen<LobbyScreen>()
                    }
                    game.addScreen(nextScreen)
                    game.setScreen<LobbyScreen>()
                }
            }
            is LobbyResult.Error -> Gdx.app.postRunnable { statusLabel.setText(result.message) }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.055f, 0.067f, 0.094f, 1f)
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
        if (VisUI.isLoaded()) VisUI.dispose()
        scope.cancel()
    }
}
