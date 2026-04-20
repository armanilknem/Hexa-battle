package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
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

    private var backgroundTexture: Texture? = null
    private lateinit var stage: Stage
    private lateinit var cardLabel: VisLabel
    private lateinit var indexLabel: VisLabel

    private val cards = listOf(
        "Goal:\nCapture the enemy Capital",
        "Your Turn:\nMove units, attack,\nsurvive",
        "Select Units:\nTap to select/deselect",
        "Movement:\nUnits move up to 2 tiles",
        "Combat:\nAttack adjacent enemies.\nStronger unit wins",
        "Combat Result:\nWinner loses difference.\n50 vs 30 = 20",
        "Merging:\nMove onto friendly unit\nto combine power",
        "Cities:\nCapture to gain troops\nper turn",
        "Capital:\nMore troop production.\nLose it = lose game",
        "Territory:\nMove to capture nearby tiles",
        "Victory:\nCapture all enemy capitals"
    )

    private val cardSprites = listOf(
        "tutorials/goal.png",
        "tutorials/turn.png",
        "tutorials/select.png",
        "tutorials/movement.png",
        "tutorials/combat.png",
        "tutorials/combat_result.png",
        "tutorials/merge.png",
        "tutorials/city.png",
        "tutorials/capital.png",
        "tutorials/territory.png",
        "tutorials/victory.png"
    )

    private var spriteTexture: Texture? = null
    private lateinit var spriteImage: Image

    private var currentCard = 0

    override fun show() {
        backgroundTexture = Texture(Gdx.files.internal("backgrounds/TutorialBackground.png"))
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
        Gdx.input.inputProcessor = stage

        cardLabel = VisLabel(cards[currentCard]).apply {
            setFontScale(1.6f)
            wrap = true
            color = Color.BLACK
            setAlignment(Align.left)
        }

        indexLabel = VisLabel("1/${cards.size}").apply {
            setFontScale(1.2f)
            color = Color.BLACK
            setAlignment(Align.center)
        }

        spriteImage = Image()
        loadSprite(currentCard)

        val prevBtn = VisTextButton("< PREV").apply {
            onClick {
                if (currentCard > 0) {
                    currentCard--
                    updateCard()
                }
            }
            color = Color.BLACK
        }

        val nextBtn = VisTextButton("NEXT >").apply {
            onClick {
                if (currentCard < cards.lastIndex) {
                    currentCard++
                    updateCard()
                }
            }
            color = Color.BLACK
        }

        val backBtn = VisTextButton("BACK TO MENU").apply {
            onClick { game.setScreen<MenuScreen>() }
            color = Color.BLACK
        }

        val root = Table().apply {
            setFillParent(true)
            top()
            background = TextureRegionDrawable(TextureRegion(backgroundTexture))
        }

        val navRow = Table()
        navRow.add(prevBtn).width(120f).height(48f).padRight(16f)
        navRow.add(indexLabel).expandX().center()
        navRow.add(nextBtn).width(120f).height(48f).padLeft(16f)

        val contentArea = Table().apply { pad(16f) }

        val textColumn = Table()
        textColumn.add(cardLabel).width(280f).top().left().expand().row()

        val spriteColumn = Table()
        spriteColumn.add(spriteImage).width(160f).height(130f).center()

        contentArea.add(textColumn).width(280f).fillY().padRight(16f)
        contentArea.add(spriteColumn).width(160f).fillY()

        val boardContent = Table()
        boardContent.add(contentArea).expandX().fillX().height(180f).row()
        boardContent.add(navRow).expandX().fillX().padTop(16f).row()

        root.pad(48f)
        root.add(boardContent).width(460f).height(210f).padTop(35f).row()
        root.add(backBtn).width(240f).height(48f).padTop(32f)

        stage.addActor(root)
    }

    private fun updateCard() {
        cardLabel.setText(cards[currentCard])
        indexLabel.setText("${currentCard + 1}/${cards.size}")
        loadSprite(currentCard)
    }

    private fun loadSprite(index: Int) {
        spriteTexture?.dispose()
        val path = cardSprites.getOrNull(index)
        if (path != null && Gdx.files.internal(path).exists()) {
            spriteTexture = Texture(Gdx.files.internal(path))
            spriteImage.drawable = TextureRegionDrawable(TextureRegion(spriteTexture))
        } else {
            spriteImage.drawable = null
        }
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
        spriteTexture?.dispose()
    }
}
