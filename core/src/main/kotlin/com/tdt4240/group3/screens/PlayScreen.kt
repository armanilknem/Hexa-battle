package com.tdt4240.group3.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.tdt4240.group3.Hexa_Battle
import com.tdt4240.group3.game.playstate.PlaySubState
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TroopComponent
import com.tdt4240.group3.model.entities.EntityFactory
import com.tdt4240.group3.model.systems.SelectionSystem
import com.tdt4240.group3.model.systems.TurnSystem
import com.tdt4240.group3.states.playstate.EnemyTurnState
import com.tdt4240.group3.states.playstate.PlayerTurnState
import ktx.app.KtxScreen
import ktx.graphics.use

class PlayScreen(private val game: Hexa_Battle, private val engine: Engine) : KtxScreen {

    private var currentState: PlaySubState = PlayerTurnState()
    private var previousState: PlaySubState = PlayerTurnState()
    val camera = OrthographicCamera()

    private val turnSystem      = TurnSystem()
    private val selectionSystem = SelectionSystem(turnSystem)

    init {
        camera.setToOrtho(false, Hexa_Battle.WIDTH.toFloat(), Hexa_Battle.HEIGHT.toFloat())
        camera.position.set(Hexa_Battle.WIDTH / 2f, Hexa_Battle.HEIGHT / 2f, 0f)

        val factory = EntityFactory(engine)
        factory.generateRectangularGrid(18, 15)
        factory.createCity(
            name = "Manchester", isCapital = true, baseProduction = 20,
            q = 3, r = 3, team = TeamComponent.TeamName.RED
        )
        factory.createTroop(team = TeamComponent.TeamName.BLUE, strength = 10, q = 5, r = 5)
        factory.createTroop(team = TeamComponent.TeamName.RED,  strength = 10, q = 3, r = 7)

        engine.addSystem(turnSystem)
        engine.addSystem(selectionSystem)

        selectionSystem.onTurnEnd = {
            when (turnSystem.currentTeam) {
                TeamComponent.TeamName.BLUE -> changeState(PlayerTurnState())
                TeamComponent.TeamName.RED  -> changeState(EnemyTurnState())
                else -> {}
            }
        }
        currentState.enter(this)
    }

    override fun show() {
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val worldCoords = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
                selectionSystem.handleTouch(worldCoords.x, worldCoords.y)
                return true
            }
        }
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
            // Display whose turn it is
            game.font.draw(
                game.batch,
                "Turn: ${turnSystem.currentTeam}",
                10f, Hexa_Battle.HEIGHT - 10f
            )
            currentState.render(this@PlayScreen)
        }
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
