package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
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

class HowToPlayScreen(private val game: Hexa_Battle) : KtxScreen {

    private lateinit var stage: Stage
    private lateinit var cardLabel: VisLabel
    private lateinit var indexLabel: VisLabel

    private val cards = listOf(
        "Goal:\nCapture the enemy Capital.",
        "Your Turn:\nMove units, attack,\nsurvive.",
        "Select Units:\nTap to select/deselect.",
        "Movement:\nUnits move up to 2 tiles.",
        "Combat:\nAttack adjacent enemies.\nStronger unit wins.",
        "Combat Result:\nWinner loses difference.\n50 vs 30 → 20",
        "Merging:\nMove onto friendly unit\nto combine power.",
        "Cities:\nCapture to gain troops\nper turn.",
        "Capital:\nMore troop production.\nLose it = lose game.",
        "Territory:\nMove to capture nearby tiles.",
        "Victory:\nCapture all enemy capitals."
    )

    private var currentCard = 0

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
        Gdx.input.inputProcessor = stage

        cardLabel = VisLabel(cards[currentCard]).apply {
            setFontScale(1.6f)
            wrap = true
        }

        indexLabel = VisLabel("1/${cards.size}").apply {
            setFontScale(1.2f)
        }

        val prevBtn = VisTextButton("< PREV").apply {
            onClick {
                if (currentCard > 0) {
                    currentCard--
                    updateCard()
                }
            }
        }

        val nextBtn = VisTextButton("NEXT >").apply {
            onClick {
                if (currentCard < cards.lastIndex) {
                    currentCard++
                    updateCard()
                }
            }
        }

        val backBtn = VisTextButton("BACK TO MENU").apply {
            onClick { game.setScreen<MenuScreen>() }
        }

        val root = Table().apply {
            setFillParent(true)
            center()
            pad(40f)
        }

        // Card text — give it a fixed width so wrap works properly
        root.add(cardLabel).width(500f).center().padBottom(40f).row()
        root.add(indexLabel).center().padBottom(24f).row()

        val navRow = Table()
        navRow.add(prevBtn).width(140f).height(56f).padRight(24f)
        navRow.add(nextBtn).width(140f).height(56f)

        root.add(navRow).padBottom(28f).row()
        root.add(backBtn).width(240f).height(56f)

        stage.addActor(root)
    }

    private fun updateCard() {
        cardLabel.setText(cards[currentCard])
        indexLabel.setText("${currentCard + 1}/${cards.size}")
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
    }
}
