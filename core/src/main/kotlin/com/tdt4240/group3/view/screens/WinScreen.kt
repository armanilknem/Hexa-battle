package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.Team
import ktx.actors.onClick
import ktx.app.KtxScreen

class WinScreen(private val game: Hexa_Battle) : KtxScreen {
    var winner: Team = Team.NONE
    var viewerTeam: Team = Team.NONE
    private lateinit var stage: Stage
    private var backgroundTexture: Texture? = null

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage
        val texture = Texture(Gdx.files.internal(backgroundPathFor(winner, viewerTeam)))
        backgroundTexture = texture

        val root = Table().apply {
            setFillParent(true)
            center()
            background = TextureRegionDrawable(TextureRegion(texture))
        }

        val resultLabel = VisLabel(labelTextFor(winner, viewerTeam)).apply { setFontScale(3.5f) }
        val teamLabel = VisLabel("Winner: ${winner.name}").apply { setFontScale(1.8f) }
        val menuBtn = VisTextButton("MAIN MENU")

        menuBtn.onClick { game.setScreen<MenuScreen>() }

        root.add(resultLabel).padBottom(16f).row()
        root.add(teamLabel).padBottom(48f).row()
        root.add(menuBtn).width(280f).height(52f).row()

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        stage.dispose()
        backgroundTexture?.dispose()
        backgroundTexture = null
    }

    private fun labelTextFor(winner: Team, viewerTeam: Team): String {
        return if (winner != Team.NONE && winner == viewerTeam) "${winner.name} WINS!" else "DEFEAT"
    }

    private fun backgroundPathFor(winner: Team, viewerTeam: Team): String {
        if (winner != viewerTeam) return "backgrounds/DefeatBackground.png"

        return when (winner) {
            Team.RED -> "backgrounds/RedWinBackground.png"
            Team.BLUE -> "backgrounds/BlueWinBackground.png"
            Team.GREEN -> "backgrounds/GreenWinBackground.png"
            Team.PURPLE -> "backgrounds/PurpleWinBackground.png"
            Team.NONE -> "backgrounds/DefeatBackground.png"
        }
    }
}
