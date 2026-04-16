package com.tdt4240.group3.view

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
import com.tdt4240.group3.model.components.CapitalComponent
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.GameStateComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent // add when ready
import com.tdt4240.group3.model.components.marker.HighlightedComponent
import com.tdt4240.group3.model.components.marker.SelectedComponent
import com.tdt4240.group3.view.styleRegistries.CityStyleRegistry
import com.tdt4240.group3.view.styleRegistries.TeamStyleRegistry
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

    private val teamStyleRegistry = TeamStyleRegistry

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
                if (tileFamily.matches(entity)) {
                    drawTileHighlight(entity)
                    drawTerritory(entity)
                }
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
                    if (entity.getComponent(CapitalComponent::class.java) != null) {
                        drawCapitalCity(entity)
                    } else drawNormalCity(entity)
                }
        }

        // Pass 2b — unmoved troop highlights (above cities, below troops)
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
        val troop = entity[TroopComponent.mapper] ?: return
        if (!troop.isHighlighted) return

        val pos = entity[PositionComponent.mapper] ?: return

        // Make the highlight pulsate when selected
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
        val x = pos.x
        val y = pos.y
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
        cityName: String? = null
    ) {
        val pos = entity[PositionComponent.mapper] ?: return
        val visuals = CityStyleRegistry.getFor(entity)

        val width = visuals.width
        val height = visuals.height
        val xOffset = visuals.xOffset
        val yOffset = visuals.yOffset

        batch.draw(visuals.texture, pos.x - width / 2f + xOffset, pos.y - height / 2f + yOffset, width, height)
        if (cityName != null) {
            font.data.setScale(0.7f)
            font.draw(batch, cityName, pos.x - width / 2f + 5f, pos.y - height / 2f + yOffset)
            font.data.setScale(1f)
        }
    }

    private fun drawCapitalCity(entity: Entity) {
        val city = entity[CityComponent.mapper] ?: return
        drawCity(entity, cityName = city.name)
    }

    private fun drawNormalCity(entity: Entity) {
        drawCity(entity)
    }

    private fun drawTroop(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        val team = entity[TeamComponent.mapper] ?: return
        val troop = entity[TroopComponent.mapper] ?: return
        val strength = troop.strength

        // get texture from teamsStyleRegistry based off name and strength
        val texture = teamStyleRegistry.get(team.team, strength).troopTexture
        if (texture != null) {
            batch.draw(texture, pos.x - 8f, pos.y - 8f, 16f, 16f)
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
        val x = pos.x
        val y = pos.y
        val size = 16f

        // make territory semi-transparent for other teams
        val alpha = if (team == getCurrentTeam()) 0.6f else 0.3f

        // get team color from teamStyleRegistry
        val color = teamStyleRegistry.get(team, 20).territoryColor.cpy()
        shapeRenderer.color = color.apply { a = alpha }

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
        backgroundTexture.disposeSafely()
        teamStyleRegistry.dispose()
        CityStyleRegistry.dispose()
    }
}
