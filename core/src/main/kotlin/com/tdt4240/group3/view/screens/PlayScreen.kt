package com.tdt4240.group3.view.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.controller.PauseController
import com.tdt4240.group3.controller.SelectionController
import com.tdt4240.group3.controller.TurnController
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.view.states.PlaySubState
import com.tdt4240.group3.view.states.PlayerTurnState
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine, private val turnController: TurnController, private val pauseController: PauseController, private val selectionController: SelectionController) : KtxScreen {
    private var currentState: PlaySubState = PlayerTurnState()
    private var previousState: PlaySubState = PlayerTurnState()

    val camera = OrthographicCamera()

    private lateinit var stage: Stage
    private lateinit var turnLabel: VisLabel
    private lateinit var tooltipLabel: VisLabel

    init {
        setUpCamera()
        currentState.enter(this)
    }

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()
        setUpStage()
        setUpUI()
        setUpInput()
    }

    override fun render(delta: Float) {
        val (r, g, b) = currentState.backgroundColor
        Gdx.gl.glClearColor(r, g, b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        currentState.handleInput(this)
        currentState.update(this, delta)
        engine.update(delta)

        game.batch.projectionMatrix = camera.combined
        game.batch.use {
            currentState.render(this@PlayScreen)
        }

        updateTurnLabel()

        // Stage draws on top of the game world — must be outside batch.use
        stage.act(delta)
        stage.draw()
    }

    private fun setUpCamera() {
        camera.setToOrtho(false, Hexa_Battle.WIDTH.toFloat(), Hexa_Battle.HEIGHT.toFloat())
        camera.position.set(Hexa_Battle.WIDTH / 2f, Hexa_Battle.HEIGHT / 2f, 0f)
    }

    private fun setUpStage() {
        stage = Stage(ScreenViewport())
    }

    private fun setUpUI() {
        val root = Table().apply { setFillParent(true) }
        val gs = getGameState()

        turnLabel  = VisLabel("Team: ${gs.currentTeam}   Turn: ${gs.turnCount}")  // now a field
        turnLabel.setFontScale(2f)

        val pauseBtn   = VisTextButton("PAUSE")
        val endTurnBtn = VisTextButton("END TURN")

        pauseBtn.onClick   { togglePause() }
        endTurnBtn.onClick { turnController.endTurn() }

        root.top()
        root.add(turnLabel).expandX().left().pad(8f)
        root.add(pauseBtn).right().pad(8f)
        root.add(endTurnBtn).right().pad(8f).row()

        stage.addActor(root)
    }

    private fun setUpInput() {
        setupTooltip()

        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val worldCoords =
                    camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
                selectionController.handleTouch(worldCoords.x, worldCoords.y)
                return true
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                updateTooltip(screenX, screenY)
                return false
            }
        })
        Gdx.input.inputProcessor = inputMultiplexer
    }
    private fun updateTurnLabel() {
        val gameState = engine.getEntitiesFor(allOf(GameStateComponent::class).get()).firstOrNull()
        val gs = gameState?.get(GameStateComponent.mapper)!!
        turnLabel.setText("Team: ${gs.currentTeam}   Turn: ${gs.turnCount}")
    }

    private fun setupTooltip() {
        tooltipLabel = VisLabel("").apply {
            isVisible = false
            setFontScale(1.2f)
            color = Color.BLACK
        }
        stage.addActor(tooltipLabel)
    }

    private fun updateTooltip(screenX: Int, screenY: Int) {
        val worldCoords = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val tile = selectionController.findTileAt(worldCoords.x, worldCoords.y)
        val pos = tile?.get(PositionComponent.mapper)
        if (pos != null) {
            val city = selectionController.findCityAt(worldCoords.x, worldCoords.y)
            val cityName = city?.get(CityComponent.mapper)?.name
            val text = if (cityName != null) "q: ${pos.q}, r: ${pos.r}\n$cityName"
            else "q: ${pos.q}, r: ${pos.r}"
            tooltipLabel.setText(text)
            tooltipLabel.pack()
            tooltipLabel.setPosition(
                screenX.toFloat() + 12f,
                stage.viewport.screenHeight - screenY.toFloat() + 12f
            )
            tooltipLabel.isVisible = true
        } else {
            tooltipLabel.isVisible = false
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun changeState(newState: PlaySubState) {
        currentState.exit(this)
        previousState = currentState
        currentState = newState
        currentState.enter(this)
    }

    private fun getGameState(): GameStateComponent {
        val gameStateEntity = engine.getEntitiesFor(allOf(GameStateComponent::class).get()).firstOrNull()
        return gameStateEntity?.get(GameStateComponent.mapper)
            ?: error("GameStateComponent was not found")
    }

    private fun togglePause() {
        changeState(pauseController.togglePause(currentState, previousState))
    }

    fun goToMenu() { game.setScreen<MenuScreen>() }
    fun getBatch() = game.batch
    fun getFont()  = game.font

    override fun dispose() { super.dispose() }
}
