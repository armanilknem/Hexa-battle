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
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.components.marker.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.disposeSafely
import ktx.graphics.use
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.model.Team
import com.tdt4240.group3.view.styleRegistries.CityStyleRegistry
import com.tdt4240.group3.view.styleRegistries.TeamVisualRegistry

class View(
    private val batch: SpriteBatch,
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera,
    private val font: BitmapFont
) : EntitySystem(), Disposable {
    private var stateTime = 0f
    private val backgroundTexture = Texture(Gdx.files.internal("backgrounds/GameBackground.png"))

    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()
    private val gameStateFamily = allOf(GameStateComponent::class).get()

    override fun update(deltaTime: Float) {
        stateTime += deltaTime

        batch.projectionMatrix = batch.projectionMatrix.idt() // identity projection
        batch.use {
            drawBackground()
        }

        shapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            engine.getEntitiesFor(tileFamily).forEach { drawTerritory(it) }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        shapeRenderer.use(ShapeRenderer.ShapeType.Line) {
            engine.getEntitiesFor(tileFamily).forEach { drawTile(it) }
        }

        batch.projectionMatrix = camera.combined
        batch.use {
            engine.getEntitiesFor(cityFamily)
                .sortedBy { it[PositionComponent.mapper]?.zIndex ?: 0 }
                .forEach { entity ->
                    if (entity[CapitalComponent.mapper] != null) drawCapitalCity(entity)
                    else drawNormalCity(entity)
                }
        }

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            engine.getEntitiesFor(tileFamily).forEach { drawTileHighlight(it) }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            engine.getEntitiesFor(troopFamily).forEach { drawUnmovedTroopHighlight(it) }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = camera.combined
        batch.use {
            engine.getEntitiesFor(troopFamily)
                .sortedBy { it[PositionComponent.mapper]?.zIndex ?: 0 }
                .forEach { drawTroop(it) }
        }
    }

    private fun drawTileHighlight(entity: Entity) {
        if (entity[HighlightedComponent.mapper] == null) return
        val pos = entity[PositionComponent.mapper] ?: return
        shapeRenderer.color = Color(1f, 1f, 1f, 0.5f)
        drawFullHexTile(pos.x, pos.y, GameConstants.HEX_SIZE)
    }

    private fun drawUnmovedTroopHighlight(entity: Entity) {
        if (entity[HighlightedComponent.mapper] == null) return
        val pos = entity[PositionComponent.mapper] ?: return
        val alpha = if (entity[SelectedComponent.mapper] != null)
            0.55f + 0.25f * sin(stateTime * 4.0).toFloat()
        else
            0.6f
        shapeRenderer.color = Color(1f, 0.85f, 0f, alpha)
        drawFullHexTile(pos.x, pos.y, GameConstants.HEX_SIZE + 2f)
    }

    private fun drawTile(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        val x = pos.x
        val y = pos.y

        shapeRenderer.color = Color.BLACK
        Gdx.gl.glLineWidth(3f)

        for (i in 0 until 6) {
            val angle1 = (PI / 180) * (60 * i - 30)
            val angle2 = (PI / 180) * (60 * (i + 1) - 30)
            shapeRenderer.line(
                x + GameConstants.HEX_SIZE * cos(angle1).toFloat(),
                y + GameConstants.HEX_SIZE * sin(angle1).toFloat(),
                x + GameConstants.HEX_SIZE * cos(angle2).toFloat(),
                y + GameConstants.HEX_SIZE * sin(angle2).toFloat()
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

        val texture = TeamVisualRegistry.getTexture(team.team, strength)
        batch.draw(texture, pos.x - 14f, pos.y - 13.5f, 30f, 32f)

        font.draw(
            batch,
            troop.strength.toString(),
            pos.x - 6f,
            pos.y + 20f
        )
    }

    private fun drawTerritory(entity: Entity) {
        val team = entity[TeamComponent.mapper]?.team ?: return
        if (team == Team.NONE) return

        val pos = entity[PositionComponent.mapper] ?: return
        val alpha = if (team == getCurrentTeam()) 0.7f else 0.5f
        val color = TeamVisualRegistry.getColor(team)
        shapeRenderer.color = color.apply { a = alpha }
        drawFullHexTile(pos.x, pos.y, GameConstants.HEX_SIZE)
    }

    private fun getCurrentTeam(): Team {
        val gameState = engine.getEntitiesFor(gameStateFamily).firstOrNull()
        return gameState?.get(GameStateComponent.mapper)?.currentTeam
            ?: Team.NONE
    }

    private fun drawFullHexTile(x: Float, y: Float, size: Float) {
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

    private fun drawBackground() {
        batch.draw(backgroundTexture, -1f, -1f, 2f, 2f)
    }

    override fun dispose() {
        backgroundTexture.disposeSafely()
    }
}
