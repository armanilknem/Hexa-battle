package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.network.LobbyService
import com.tdt4240.group3.view.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class LeaderboardScreen(private val game: Hexa_Battle) : KtxScreen {

    private var backgroundTexture: Texture? = null
    private var spriteTexture: Texture? = null
    private lateinit var stage: Stage
    private lateinit var leaderboardTable: Table

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun show() {
        backgroundTexture = Texture(Gdx.files.internal("backgrounds/TutorialBackground.png"))
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
        Gdx.input.inputProcessor = stage

        val titleLabel = VisLabel("LEADERBOARD").apply {
            setFontScale(2f)
            color = Color.BLACK
            setAlignment(Align.center)
        }

        leaderboardTable = Table()
        showLoadingState()

        val backBtn = VisTextButton("BACK TO MENU").apply {
            onClick { game.setScreen<MenuScreen>() }
            color = Color.BLACK
        }

        val root = Table().apply {
            setFillParent(true)
            top()
            background = TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val contentArea = Table().apply {
            pad(16f)
            padBottom(32f)
        }

        val textColumn = Table()
        textColumn.add(titleLabel).width(280f).padBottom(12f).left().row()
        textColumn.add(leaderboardTable).width(280f).top().left().expand().row()


        contentArea.add(textColumn).width(280f).fillY()

        val boardContent = Table()
        boardContent.add(contentArea).expandX().fillX().height(180f).row()

        root.pad(48f)
        root.add(boardContent).width(460f).height(210f).padTop(35f).row()
        root.add(backBtn).width(240f).height(48f).padTop(32f)

        stage.addActor(root)

        fetchLeaderboard()
    }

    private fun showLoadingState() {
        leaderboardTable.clear()
        leaderboardTable.add(VisLabel("Loading...").apply {
            setFontScale(1.2f)
            color = Color.BLACK
        }).left().row()
    }

    private fun fetchLeaderboard() {
        scope.launch {
            val topWinners = LobbyService.getTopWinners(5)

            Gdx.app.postRunnable {
                leaderboardTable.clear()

                if (topWinners.isEmpty()) {
                    leaderboardTable.add(VisLabel("No games played yet.").apply {
                        setFontScale(1.2f)
                        color = Color.BLACK
                    }).left().row()
                    return@postRunnable
                }

                val headerRank = VisLabel("#").apply { setFontScale(1.2f); color = Color.BLACK }
                val headerName = VisLabel("PLAYER").apply { setFontScale(1.2f); color = Color.BLACK }
                val headerWins = VisLabel("WINS").apply { setFontScale(1.2f); color = Color.BLACK }
                leaderboardTable.add(headerRank).width(30f).left().padBottom(6f)
                leaderboardTable.add(headerName).width(190f).left().padBottom(6f)
                leaderboardTable.add(headerWins).width(50f).left().padBottom(6f).row()

                topWinners.forEachIndexed { index, (name, wins) ->
                    val rank  = VisLabel("${index + 1}").apply { setFontScale(1.2f); color = Color.BLACK }
                    val nameL = VisLabel(name).apply { setFontScale(1.2f); color = Color.BLACK }
                    val winsL = VisLabel("$wins").apply { setFontScale(1.2f); color = Color.BLACK }

                    leaderboardTable.add(rank).width(30f).left().padBottom(4f)
                    leaderboardTable.add(nameL).width(190f).left().padBottom(4f)
                    leaderboardTable.add(winsL).width(50f).left().padBottom(4f).row()
                }
            }
        }
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
        spriteTexture?.dispose()
        scope.cancel()
    }
}
