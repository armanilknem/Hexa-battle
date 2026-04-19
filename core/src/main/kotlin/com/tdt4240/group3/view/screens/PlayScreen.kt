package com.tdt4240.group3.view.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.controller.PauseController
import com.tdt4240.group3.controller.SelectionController
import com.tdt4240.group3.controller.TurnController
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.view.states.PlaySubState
import com.tdt4240.group3.view.states.PauseState
import com.tdt4240.group3.view.states.PlayerTurnState
import com.tdt4240.group3.model.entities.TroopFactory
import com.tdt4240.group3.network.MultiplayerManager
import com.tdt4240.group3.view.states.*
import com.tdt4240.group3.view.styleRegistries.TeamVisualRegistry
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class PlayScreen(
    private val game: Hexa_Battle,
    private val engine: Engine,
    private val turnController: TurnController,
    private val pauseController: PauseController,
    private val selectionController: SelectionController,
    private val lobbyId: Int,
    private val myPlayerId: String,
    private val troopFactory: TroopFactory
) : KtxScreen {

    private var currentState: PlaySubState = PlayerTurnState()
    private var previousState: PlaySubState = PlayerTurnState()

    val camera = OrthographicCamera()

    private lateinit var stage: Stage
    private lateinit var teamLabel: VisLabel
    private lateinit var turnCountLabel: VisLabel
    private lateinit var movesLeftLabel: VisLabel
    private lateinit var topBar: Table
    private lateinit var tooltipLabel: VisLabel
    private lateinit var pauseBtn: VisTextButton
    private lateinit var endTurnBtn: VisTextButton
    private lateinit var pauseOverlay: Table

    private var multiplayerManager: MultiplayerManager? = null

    init {
        setUpCamera()
        currentState.enter(this)
    }

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()
        setUpStage()
        setUpUI()
        setUpInput()

        val gs = getGameState()
        val isMyTurn = gs.playerOrder.isNotEmpty() && gs.playerOrder[gs.currentPlayerIndex] == myPlayerId
        onTurnChanged(isMyTurn)

        multiplayerManager = MultiplayerManager(
            lobbyId = lobbyId,
            myPlayerId = myPlayerId,
            engine = engine,
            screen = this,
            troopFactory = troopFactory
        ).also { it.start() }
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
        updateTopBarColor()
        updateUiForState()

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
        val root = Table().apply {
            setFillParent(true)
            top()
            pad(8f)
        }
        val gs = getGameState()

        topBar = Table().apply {
            pad(8f)
        }

        teamLabel = VisLabel("Team: ${gs.currentTeam}").apply {
            setFontScale(2.8f)
            color = Color.BLACK
        }

        turnCountLabel = VisLabel("Turn: ${gs.turnCount}").apply {
            setFontScale(2.8f)
            color = Color.BLACK
        }

        movesLeftLabel = VisLabel("Moves: ${gs.movesLeft}").apply {
            setFontScale(2.8f)
            color = Color.BLACK
        }

        val infoContainer = Table()
        infoContainer.add(teamLabel).width(320f).left().padRight(12f)
        infoContainer.add(turnCountLabel).width(230f).left().padRight(12f)
        infoContainer.add(movesLeftLabel).width(260f).left()

        pauseBtn = VisTextButton("PAUSE").apply {
            label.setFontScale(1.6f)
            pad(6f, 14f, 6f, 14f)
            onClick { togglePause() }
        }

        endTurnBtn = VisTextButton("END TURN").apply {
            label.setFontScale(1.6f)
            pad(6f, 14f, 6f, 14f)
            onClick { turnController.endTurn() }
        }

        val buttonGroup = Table().apply {
            add(pauseBtn).height(70f).padRight(10f)
            add(endTurnBtn).height(70f)
        }

        topBar.add(infoContainer).expandX().left()
        topBar.add(buttonGroup).right()

        root.add(topBar).growX().row()
        stage.addActor(root)

        pauseOverlay = buildPauseOverlay().apply { isVisible = false }
        stage.addActor(pauseOverlay)
    }

    private fun setUpInput() {
        setupTooltip()

        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (currentState is PauseState || currentState is EnemyTurnState) return false

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

        teamLabel.setText("Team: ${gs.currentTeam}")
        turnCountLabel.setText("Turn: ${gs.turnCount}")
        movesLeftLabel.setText("Moves: ${gs.movesLeft}")
    }

    private fun updateTopBarColor() {
        val gs = getGameState()

        // Get base color from the catalog
        val base = TeamVisualRegistry.getColor(gs.currentTeam)

        val bgColor = Color(base.r, base.g, base.b, 0.55f)
        topBar.background = VisUI.getSkin().newDrawable("white", bgColor)

        val textColor = chooseContrastingTextColor(base)
        teamLabel.color = textColor
        turnCountLabel.color = textColor
        movesLeftLabel.color = textColor
    }

    private fun chooseContrastingTextColor(bg: Color): Color {
        // Perceived brightness (WCAG standard)
        val luminance = 0.299f * bg.r + 0.587f * bg.g + 0.114f * bg.b

        return if (luminance > 0.55f) Color.BLACK else Color.WHITE
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

    private fun updateUiForState() {
        val isPaused = currentState is PauseState
        val isMyTurn = currentState is PlayerTurnState
        endTurnBtn.isVisible = !isPaused && isMyTurn
        endTurnBtn.touchable = if (!isPaused && isMyTurn) Touchable.enabled else Touchable.disabled

        // Hide the top UI bar when paused
        topBar.isVisible = !isPaused
        topBar.touchable = if (isPaused) Touchable.disabled else Touchable.enabled

        // Pause overlay
        pauseOverlay.isVisible = isPaused
        pauseOverlay.touchable = if (isPaused) Touchable.enabled else Touchable.disabled
        if (isPaused) {
            tooltipLabel.isVisible = false
        }
    }

    private fun buildPauseOverlay(): Table {
        val overlay = Table().apply {
            setFillParent(true)
            center()
        }

        val pauseTitle = VisLabel("PAUSED").apply { setFontScale(3f) }
        val resumeButton = createShadowButton("RESUME") { resumeGame() }
        val menuButton = createShadowButton("MAIN MENU") { goToMenu() }

        overlay.add(pauseTitle).padBottom(28f).row()
        overlay.add(resumeButton).width(300f).height(64f).padBottom(18f).row()
        overlay.add(menuButton).width(300f).height(64f)

        return overlay
    }

    private fun createShadowButton(text: String, onClickAction: () -> Unit): Actor {
        val button = VisTextButton(text)
        button.onClick { onClickAction() }

        val shadow = VisTextButton(text).apply {
            color = Color(0f, 0f, 0f, 0.35f)
            isDisabled = true
            touchable = Touchable.disabled
        }

        val stack = Stack().apply {
            add(Table().apply {
                add(shadow).expand().fill().padLeft(6f).padTop(6f)
            })
            add(Table().apply {
                add(button).expand().fill().padRight(6f).padBottom(6f)
            })
        }

        return stack
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun onTurnChanged(isMyTurn: Boolean) {
        val newState = if (isMyTurn) PlayerTurnState() else EnemyTurnState()
        if (currentState is PauseState) {
            previousState = newState
        } else {
            changeState(newState)
        }
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

    fun resumeGame() {
        if (currentState is PauseState) {
            togglePause()
        }
    }

    fun goToMenu() { game.setScreen<MenuScreen>() }
    fun goToWin(winner: Team) {
        val winScreen = game.getScreen<WinScreen>()
        winScreen.winner = winner
        winScreen.viewerTeam = game.myTeam
        game.setScreen<WinScreen>()
    }
    fun getBatch() = game.batch
    fun getFont()  = game.font

    override fun dispose() { super.dispose() }
}
