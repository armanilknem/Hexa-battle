package com.tdt4240.group3.view.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.controller.PauseController
import com.tdt4240.group3.controller.SelectionController
import com.tdt4240.group3.controller.TurnController
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.view.ViewConfig
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

    // Separate viewport for the game world (hex map)
    private lateinit var worldViewport: ExtendViewport

    private lateinit var stage: Stage
    private lateinit var teamLabel: VisLabel
    private lateinit var turnCountLabel: VisLabel
    private lateinit var movesLeftLabel: VisLabel
    private lateinit var topBar: Table
    private lateinit var tooltipLabel: VisLabel
    private lateinit var pauseBtn: VisTextButton
    private lateinit var endTurnBtn: VisTextButton
    private lateinit var pauseOverlay: Table

    // All sizing derived from V_HEIGHT so it scales consistently on any device
    private val barH      = ViewConfig.V_HEIGHT * 0.08f
    private val fontScale = ViewConfig.V_HEIGHT * 0.0028f
    private val btnFontSc = ViewConfig.V_HEIGHT * 0.0020f
    private val btnH      = ViewConfig.V_HEIGHT * 0.075f
    private val btnW      = ViewConfig.V_WIDTH  * 0.15f
    private val padSm     = ViewConfig.V_HEIGHT * 0.010f
    private val padMed    = ViewConfig.V_HEIGHT * 0.015f

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

        // Apply world viewport so camera projects the hex map correctly
        worldViewport.apply()
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
        // ExtendViewport manages camera dimensions — do not call setToOrtho here.
        // The camera position is set externally (LobbyScreen) after construction.
        worldViewport = ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT, camera)
    }

    private fun setUpStage() {
        // UI stage uses its own ExtendViewport with the same virtual resolution.
        // Buttons/labels positioned in virtual coords look identical on every device.
        stage = Stage(ExtendViewport(ViewConfig.V_WIDTH, ViewConfig.V_HEIGHT))
    }

    private fun setUpUI() {
        val root = Table().apply {
            setFillParent(true)
            top()
        }

        topBar = Table().apply {
            pad(padSm)
        }

        val gs = getGameState()

        teamLabel = VisLabel("Team: ${gs.currentTeam}").apply {
            setFontScale(fontScale)
        }
        turnCountLabel = VisLabel("Turn: ${gs.turnCount}").apply {
            setFontScale(fontScale)
        }
        movesLeftLabel = VisLabel("Moves: ${gs.movesLeft}").apply {
            setFontScale(fontScale)
        }

        val infoContainer = Table()
        infoContainer.add(teamLabel).expandX().left().padRight(padMed)
        infoContainer.add(turnCountLabel).expandX().left().padRight(padMed)
        infoContainer.add(movesLeftLabel).expandX().left()

        pauseBtn = VisTextButton("PAUSE").apply {
            label.setFontScale(btnFontSc)
            pad(padSm)
            onClick { togglePause() }
        }

        endTurnBtn = VisTextButton("END TURN").apply {
            label.setFontScale(btnFontSc)
            pad(padSm)
            onClick { turnController.endTurn() }
        }

        val buttonGroup = Table().apply {
            add(pauseBtn).width(btnW).height(btnH).padRight(padSm)
            add(endTurnBtn).width(btnW).height(btnH)
        }

        topBar.add(infoContainer).expandX().fillX().height(barH).padRight(padMed)
        topBar.add(buttonGroup).right().height(barH)

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

        val base = TeamVisualRegistry.getColor(gs.currentTeam)

        val bgColor = Color(base.r, base.g, base.b, 0.55f)
        topBar.background = VisUI.getSkin().newDrawable("white", bgColor)

        val textColor = chooseContrastingTextColor(base)
        teamLabel.color = textColor
        turnCountLabel.color = textColor
        movesLeftLabel.color = textColor
    }

    private fun chooseContrastingTextColor(bg: Color): Color {
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

            // Convert raw screen coords to stage virtual coords
            val stageCoords = stage.screenToStageCoordinates(
                Vector2(screenX.toFloat(), screenY.toFloat())
            )
            tooltipLabel.setPosition(stageCoords.x + 12f, stageCoords.y + 12f)
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

        topBar.isVisible = !isPaused
        topBar.touchable = if (isPaused) Touchable.disabled else Touchable.enabled

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

        val btnW = ViewConfig.V_WIDTH * 0.45f
        val btnH = ViewConfig.V_HEIGHT * 0.14f

        val pauseTitle = VisLabel("PAUSED").apply { setFontScale(ViewConfig.V_HEIGHT * 0.008f) }
        val resumeButton = createShadowButton("RESUME") { resumeGame() }
        val menuButton = createShadowButton("MAIN MENU") { goToMenu() }

        overlay.add(pauseTitle).padBottom(ViewConfig.V_HEIGHT * 0.06f).row()
        overlay.add(resumeButton).width(btnW).height(btnH).padBottom(ViewConfig.V_HEIGHT * 0.04f).row()
        overlay.add(menuButton).width(btnW).height(btnH)

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
        // false = don't reset camera position (it was set externally to map center)
        worldViewport.update(width, height, false)
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

    fun goToMenu() {
        if (currentState is PlayerTurnState) turnController.endTurn()
        game.setScreen<MenuScreen>()
    }
    fun goToWin(winner: Team) {
        val winScreen = game.getScreen<WinScreen>()
        winScreen.winner = winner
        winScreen.viewerTeam = game.myTeam
        game.setScreen<WinScreen>()
    }

    fun goToEliminated() {
        val winScreen = game.getScreen<WinScreen>()
        winScreen.winner = Team.NONE
        winScreen.viewerTeam = game.myTeam
        game.setScreen<WinScreen>()
    }
    fun getBatch() = game.batch
    fun getFont()  = game.font

    override fun dispose() { super.dispose() }
}
