package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.components.TeamComponent
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class WinScreen(private val game: Hexa_Battle) : KtxScreen {
    var winner: TeamComponent.TeamName = TeamComponent.TeamName.NONE
    private lateinit var stage: Stage

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val root = Table().apply { setFillParent(true); center() }

        val winLabel = VisLabel("${winner.name} WINS!").apply { setFontScale(3f) }
        val menuBtn = VisTextButton("MAIN MENU")

        menuBtn.onClick { game.setScreen<MenuScreen>() }

        root.add(winLabel).padBottom(48f).row()
        root.add(menuBtn).width(280f).height(52f).row()

        stage.addActor(root)
    }

    override fun render(delta: Float) {
        val (r, g, b) = if (winner == TeamComponent.TeamName.RED) Triple(0.4f, 0.1f, 0.1f)
                        else Triple(0.1f, 0.2f, 0.4f)
        clearScreen(r, g, b, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
        stage.dispose()
    }
}
