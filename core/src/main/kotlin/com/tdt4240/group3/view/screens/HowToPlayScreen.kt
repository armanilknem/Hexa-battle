package com.tdt4240.group3.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.tdt4240.group3.Hexa_Battle
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class HowToPlayScreen(private val game: Hexa_Battle) : KtxScreen {
    private val viewport = ExtendViewport(800f, 480f)
    private val camera = OrthographicCamera()

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

    private val backButton = Rectangle(
        Gdx.graphics.width - 150f,
        Gdx.graphics.height - 80f,
        130f,
        50f
    )

    override fun render(delta: Float) {
        clearScreen(0.2f, 0.2f, 0.15f, 1f)

        handleInput()

        game.batch.use { batch ->

            // Draw current tutorial card (centered-ish)
            game.font.draw(
                batch,
                cards[currentCard],
                100f,
                Gdx.graphics.height / 2f
            )

            // Draw card index
            game.font.draw(
                batch,
                "${currentCard + 1}/${cards.size}",
                Gdx.graphics.width / 2f - 20f,
                50f
            )

            // Draw BACK button
            game.font.color = Color.WHITE
            game.font.draw(
                batch,
                "BACK",
                backButton.x + 30f,
                backButton.y + 35f
            )
        }
    }

    private fun handleInput() {

        // Touch input
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            // Back button click
            if (backButton.contains(touchX, touchY)) {
                game.setScreen<MenuScreen>()
                return
            }

            // Tap right side → next card
            if (touchX > Gdx.graphics.width / 2f) {
                currentCard = (currentCard + 1).coerceAtMost(cards.lastIndex)
            } else {
                // Tap left side → previous card
                currentCard = (currentCard - 1).coerceAtLeast(0)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        super.dispose()
    }
}
