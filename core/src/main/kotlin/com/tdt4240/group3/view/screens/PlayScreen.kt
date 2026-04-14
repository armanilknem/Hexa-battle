package com.tdt4240.group3.view.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
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
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.CollisionSystem
import com.tdt4240.group3.controller.systems.MovementSystem
import com.tdt4240.group3.controller.systems.SelectionSystem
import com.tdt4240.group3.controller.systems.TroopCreationSystem
import com.tdt4240.group3.controller.systems.TurnSystem
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.view.states.PlaySubState
import com.tdt4240.group3.view.states.PlayerTurnState
import com.tdt4240.group3.view.states.EnemyTurnState
import com.tdt4240.group3.view.states.PauseState
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine) : KtxScreen {

    private var currentState: PlaySubState = PlayerTurnState()
    private var previousState: PlaySubState = PlayerTurnState()
    val camera = OrthographicCamera()

    private val turnSystem      = TurnSystem()
    private val movementSystem = MovementSystem()
    private val troopCreationSystem = TroopCreationSystem(engine)
    private val selectionSystem = SelectionSystem()
    private val collisionSystem = CollisionSystem()


    private val pauseController = PauseController(turnSystem, this)

    private lateinit var stage: Stage
    private lateinit var turnLabel: VisLabel

    init {
        camera.setToOrtho(false, Hexa_Battle.WIDTH.toFloat(), Hexa_Battle.HEIGHT.toFloat())
        camera.position.set(Hexa_Battle.WIDTH / 2f, Hexa_Battle.HEIGHT / 2f, 0f)
        val factory = EntityFactory(engine)
        factory.generateRectangularGrid(18, 15)
        factory.createCity(
            name = "Manchester", isCapital = true, baseProduction = 20,
            q = 3, r = 3, team = TeamComponent.TeamName.RED
        )
        factory.createCity(
            name = "Bikini Buttom", isCapital = true, baseProduction = 20,
            q = 8, r = 7, team = TeamComponent.TeamName.BLUE
        )
        factory.createGameState()
        engine.addSystem(turnSystem)
        engine.addSystem(selectionSystem)
        engine.addSystem(movementSystem)
        engine.addSystem(collisionSystem)
        engine.addSystem(troopCreationSystem)

        troopCreationSystem.createTroopsForTeam(TeamComponent.TeamName.BLUE)

        currentState.enter(this)
    }

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ScreenViewport())

        val root = Table().apply { setFillParent(true) }

        val gameState = engine.getEntitiesFor(allOf(GameStateComponent::class).get()).firstOrNull()
        val gs = gameState?.get(GameStateComponent.mapper)!!

        turnLabel  = VisLabel("Team: ${gs.currentTeam}   Turn: ${gs.turnCount}")  // now a field
        turnLabel.setFontScale(2f)
        val pauseBtn   = VisTextButton("PAUSE")
        val endTurnBtn = VisTextButton("END TURN")

        pauseBtn.onClick   { pauseController.togglePause(currentState) }
        endTurnBtn.onClick { turnSystem.endTurn() }

        root.top()
        root.add(turnLabel).expandX().left().pad(8f)
        root.add(pauseBtn).right().pad(8f)
        root.add(endTurnBtn).right().pad(8f).row()


        stage.addActor(root)

        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(stage)
        inputMultiplexer.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val worldCoords = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
                selectionSystem.handleTouch(worldCoords.x, worldCoords.y)
                return true
            }
        })
        Gdx.input.inputProcessor = inputMultiplexer
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

        updateLabel()

        // Stage draws on top of the game world — must be outside batch.use
        stage.act(delta)
        stage.draw()
    }
    fun updateLabel() {
        val gameState = engine.getEntitiesFor(allOf(GameStateComponent::class).get()).firstOrNull()
        val gs = gameState?.get(GameStateComponent.mapper)!!
        turnLabel.setText("Team: ${gs.currentTeam}   Turn: ${gs.turnCount}")
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

    fun goToMenu() { game.setScreen<MenuScreen>() }
    fun getBatch() = game.batch
    fun getFont()  = game.font

    override fun dispose() { super.dispose() }
}
