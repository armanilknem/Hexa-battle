package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.model.team.TeamName
import com.tdt4240.group3.screens.LobbySelectScreen
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class MenuScreen(private val game: Hexa_Battle) : KtxScreen {

    private lateinit var stage: Stage
    private val backgroundTexture = Texture(Gdx.files.internal("backgrounds/MenuBackground.png"))

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        val root = Table().apply {
            setFillParent(true)
            center()
            background = TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val titleLabel  = VisLabel("HEXA·BATTLE")
        val playBtn     = VisTextButton("PLAY")
        val howToBtn    = VisTextButton("HOW TO PLAY")
        val optionsBtn  = VisTextButton("OPTIONS")

        playBtn.onClick    {
            // game.myTeam = TeamName.RED
            // game.setScreen<PlayScreen>()
            game.setScreen<LobbySelectScreen>()
        }
        howToBtn.onClick   { game.setScreen<HowToPlayScreen>() }
        optionsBtn.onClick { game.setScreen<OptionsScreen>() }

        root.add(titleLabel).padBottom(48f).row()
        root.add(playBtn).width(280f).height(52f).padBottom(12f).row()
        root.add(howToBtn).width(280f).height(52f).padBottom(12f).row()
        root.add(optionsBtn).width(280f).height(52f).row()

        stage.addActor(root)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        clearScreen(0.055f, 0.067f, 0.094f, 1f)
        stage.act(delta)
        stage.draw()
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        stage.dispose()
        backgroundTexture.dispose()
        if (VisUI.isLoaded()) VisUI.dispose()
    }
}
