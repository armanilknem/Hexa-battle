package com.tdt4240.group3.view.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.ecs.components.*
import com.tdt4240.group3.model.ecs.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.disposeSafely
import ktx.graphics.use
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class View(
    private val batch: SpriteBatch,
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera,
    private val font: BitmapFont
) : EntitySystem(), Disposable {
    private var stateTime = 0f
    private val backgroundTexture = Texture(Gdx.files.internal("hexaBackground.png"))

    // Three distinct families — one per entity type
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class).get()

    private val capitalCityTexture  = Texture(Gdx.files.internal("CapitalCity.png"))
    private val normalCityTexture = Texture(Gdx.files.internal("NormalCity.png"))

    private val troopTexture = Texture(Gdx.files.internal("troop.png"))

    private val redTroopTexture = Texture(Gdx.files.internal("red_troop.png"))

    private val blueTroopTexture = Texture(Gdx.files.internal("blue_troop.png"))

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        val entities = engine.entities

        batch.projectionMatrix = batch.projectionMatrix.idt() // identity projection
        batch.use {
            drawBackground()
        }

        shapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            entities.forEach { entity ->
                if (tileFamily.matches(entity)) drawTerritory(entity)
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        // Pass 1b — hex outlines
        shapeRenderer.use(ShapeRenderer.ShapeType.Line) {
            entities.forEach { entity ->
                if (tileFamily.matches(entity)) drawTile(entity)
            }
        }

        // Pass 2a — city sprites
        batch.projectionMatrix = camera.combined
        batch.use {
            entities
                .filter { cityFamily.matches(it) }
                .sortedBy { it[PositionComponent.mapper]?.zIndex ?: 0 }
                .forEach { entity ->
                    val city = entity[CityComponent.mapper]
                    if (city?.isCapital == true) drawCapitalCity(entity)
                    else drawNormalCity(entity)
                }
        }

        // Pass 2b — tile highlights for possible moves (above cities)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            entities.forEach { entity ->
                if (tileFamily.matches(entity)) drawTileHighlight(entity)
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        // Pass 2c — unmoved troop highlights (above cities, below troops)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            entities.forEach { entity ->
                if (troopFamily.matches(entity)) drawUnmovedTroopHighlight(entity)
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        // Pass 2c — troop sprites (above highlights)
        batch.projectionMatrix = camera.combined
        batch.use {
            entities
                .filter { troopFamily.matches(it) }
                .sortedBy { it[PositionComponent.mapper]?.zIndex ?: 0 }
                .forEach { entity -> drawTroop(entity) }
        }
    }

    private fun drawTileHighlight(entity: Entity) {
        val tile = entity[TileComponent.mapper] ?: return
        if (entity.getComponent(HighlightedComponent::class.java) == null) {return}
        val pos = entity[PositionComponent.mapper] ?: return

        shapeRenderer.color = Color(1f, 1f, 1f, 0.5f)
        val size = 16f
        val x = pos.x
        val y = pos.y

        for (i in 0 until 6) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)
            shapeRenderer.triangle(
                x, y,
                x + size * cos(angle1).toFloat(), y + size * sin(angle1).toFloat(),
                x + size * cos(angle2).toFloat(), y + size * sin(angle2).toFloat()
            )
        }
    }

    private fun drawUnmovedTroopHighlight(entity: Entity) {
        if (entity.getComponent(HighlightedComponent::class.java) == null) return

        val pos = entity[PositionComponent.mapper] ?: return

        val alpha = if (entity.getComponent(SelectedComponent::class.java) != null)
            0.55f + 0.25f * sin(stateTime * 4.0).toFloat()
        else
            0.6f
        shapeRenderer.color = Color(1f, 0.85f, 0f, alpha)

        val size = 18f
        val x = pos.x
        val y = pos.y
        for (i in 0 until 6) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)
            shapeRenderer.triangle(
                x, y,
                x + size * cos(angle1).toFloat(), y + size * sin(angle1).toFloat(),
                x + size * cos(angle2).toFloat(), y + size * sin(angle2).toFloat()
            )
        }
    }

    private fun drawTile(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        val x = pos.x.toFloat()
        val y = pos.y.toFloat()
        val size = 16f

        shapeRenderer.color = Color.BLACK
        Gdx.gl.glLineWidth(2f)

        for (i in 0 until 6) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)
            shapeRenderer.line(
                x + size * cos(angle1).toFloat(),
                y + size * sin(angle1).toFloat(),
                x + size * cos(angle2).toFloat(),
                y + size * sin(angle2).toFloat()
            )
        }
    }

    private fun drawCity(
        entity: Entity,
        texture: Texture,
        width: Float,
        height: Float,
        xOffset: Float,
        yOffset: Float,
        cityName: String? = null
    ) {
        val pos = entity[PositionComponent.mapper] ?: return
        batch.draw(texture, pos.x - width / 2f + xOffset, pos.y - height / 2f + yOffset, width, height)
        if (cityName != null) {
            font.data.setScale(0.7f)
            font.draw(batch, cityName, pos.x - width / 2f + 5f, pos.y - height / 2f + yOffset)
            font.data.setScale(1f)
        }
    }

    private fun drawCapitalCity(entity: Entity) {
        val city = entity[CityComponent.mapper] ?: return
        drawCity(entity, capitalCityTexture, width = 42f, height = 42f, xOffset = 1f, yOffset = 5.5f, cityName = city.name)
    }

    private fun drawNormalCity(entity: Entity) {
        drawCity(entity, normalCityTexture, width = 68.5f, height = 72f, xOffset = 0f, yOffset = 1.5f)
    }

    private fun drawTroop(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        val team = entity[TeamComponent.mapper] ?: return
        val troop = entity[TroopComponent.mapper] ?: return

        if (team.team == TeamComponent.TeamName.RED) {
            batch.draw(redTroopTexture, pos.x - 8f, pos.y - 8f, 16f, 16f)
        } else if (team.team == TeamComponent.TeamName.BLUE) {
            batch.draw(blueTroopTexture, pos.x - 8f, pos.y - 8f, 16f, 16f)
        }

        font.draw(
            batch,
            troop.strength.toString(),
            pos.x - 6f,
            pos.y + 20f
        )
    }

    private fun drawTerritory(entity: Entity) {
        val team = entity[TeamComponent.mapper]?.team ?: return
        if (team == TeamComponent.TeamName.NONE) return

        val pos = entity[PositionComponent.mapper] ?: return
        val x = pos.x.toFloat()
        val y = pos.y.toFloat()
        val size = 16f

        val alpha = if (team == getCurrentTeam()) 0.6f else 0.3f

        shapeRenderer.color = when (team) {
            TeamComponent.TeamName.RED -> Color(1f, 0.2f, 0.2f, alpha)
            TeamComponent.TeamName.BLUE -> Color(0.2f, 0.45f, 1f, alpha)
            TeamComponent.TeamName.NONE -> return
        }
        this.drawFullHexTile(x, y, size)
    }

    private fun getCurrentTeam(): TeamComponent.TeamName {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull()
        return gameState?.get(GameStateComponent.mapper)?.currentTeam
            ?: TeamComponent.TeamName.NONE
    }

    private fun drawFullHexTile(x: Float, y: Float, size: Float) {
        for (i in 0 until 6) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)

            shapeRenderer.triangle(
                x, y, // Center point
                x + size * cos(angle1).toFloat(), y + size * sin(angle1).toFloat(), // Vertex 1
                x + size * cos(angle2).toFloat(), y + size * sin(angle2).toFloat()  // Vertex 2
            )
        }
    }

    private fun drawBackground() {
        batch.draw(backgroundTexture, -1f, -1f, 2f, 2f) // Using identity projection: drawing from (-1, -1) to (1, 1) fills the entire screen
    }

    override fun dispose() {
        capitalCityTexture.disposeSafely()
        normalCityTexture.disposeSafely()
        troopTexture.disposeSafely()
        backgroundTexture.disposeSafely()
        redTroopTexture.disposeSafely()
        blueTroopTexture.disposeSafely()
    }
}
