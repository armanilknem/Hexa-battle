package com.tdt4240.group3.screens

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
import com.tdt4240.group3.controller.TroopCreationController
import com.tdt4240.group3.controller.TurnController
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.SelectionSystem
import com.tdt4240.group3.model.systems.TroopCreationSystem
import com.tdt4240.group3.model.systems.TroopHighlightSystem
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.states.playstate.EnemyTurnState
import com.tdt4240.group3.states.playstate.PauseState
import com.tdt4240.group3.states.playstate.PlayerTurnState
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine) : KtxScreen {

    private var currentState: PlaySubState = PlayerTurnState()
    private var previousState: PlaySubState = PlayerTurnState()
    val camera = OrthographicCamera()

    private val turnSystem      = TurnSystem()
    private val troopCreationSystem = TroopCreationSystem(engine)
    private val troopHighlightSystem = TroopHighlightSystem(turnSystem)

    private val troopCreationController = TroopCreationController(troopCreationSystem, turnSystem)


    private val turnController = TurnController(turnSystem, this, troopCreationController)
    private val selectionSystem = SelectionSystem(turnSystem)


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
        engine.addSystem(turnSystem)
        engine.addSystem(selectionSystem)
        engine.addSystem(troopCreationSystem)
        engine.addSystem(troopHighlightSystem)

        troopCreationSystem.createTroopsForTeam(turnSystem.currentTeam)


        selectionSystem.onTurnEnd = {
            turnController.endTurn()
        }
        currentState.enter(this)
    }

    override fun show() {
        if (!VisUI.isLoaded()) VisUI.load()

        stage = Stage(ScreenViewport())

        val root = Table().apply { setFillParent(true) }

        turnLabel  = VisLabel("Team: ${turnSystem.currentTeam}   Turn: ${turnSystem.turnCount}")  // now a field
        turnLabel.setFontScale(2f)
        val pauseBtn   = VisTextButton("PAUSE")
        val endTurnBtn = VisTextButton("END TURN")

        pauseBtn.onClick   { pauseController.togglePause(currentState) }
        endTurnBtn.onClick { turnController.endTurn() }

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

        // Stage draws on top of the game world — must be outside batch.use
        stage.act(delta)
        stage.draw()
    }
    fun updateLabel() {
        turnLabel.setText("Team: ${turnSystem.currentTeam}   Turn: ${turnSystem.turnCount}")
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
