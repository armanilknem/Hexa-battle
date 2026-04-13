package com.tdt4240.group3.view.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TeamComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent // add when ready
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
    private val camera: OrthographicCamera
) : EntitySystem(), Disposable {
    private val backgroundTexture = Texture(Gdx.files.internal("hexaBackground.png"))

    // Three distinct families — one per entity type
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()

    private val cityTexture  = Texture(Gdx.files.internal("Manchester_City_FC_badge.svg.png"))
    private val troopTexture = Texture(Gdx.files.internal("troop.png"))

    private val redTroopTexture = Texture(Gdx.files.internal("red_troop.png"))

    private val blueTroopTexture = Texture(Gdx.files.internal("blue_troop.png"))

    override fun update(deltaTime: Float) {
        val entities = engine.entities.toList()

        batch.projectionMatrix = batch.projectionMatrix.idt() // identity projection
        batch.use {
            drawBackground()
        }

        shapeRenderer.projectionMatrix = camera.combined

        // Pass 1a — filled highlights and territory
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            entities.forEach { entity ->
                if (tileFamily.matches(entity)) {
                    drawTerritory(entity)
                    drawTileHighlight(entity)
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

        // Pass 2 — sprites
        batch.projectionMatrix = camera.combined
        batch.use {
            entities
                .filter { cityFamily.matches(it) || troopFamily.matches(it) }
                .sortedBy { it[PositionComponent.mapper]?.zIndex ?: 0 }
                .forEach { entity ->
                    when {
                        cityFamily.matches(entity)  -> drawCity(entity)
                        troopFamily.matches(entity) -> drawTroop(entity)
                    }
                }
        }
    }

    private fun drawTileHighlight(entity: Entity) {
        val tile = entity[TileComponent.mapper] ?: return
        if (!tile.isHighlighted) return
        val pos = entity[PositionComponent.mapper] ?: return

        shapeRenderer.color = Color(1f, 1f, 1f, 0.5f)
        val size = 16f
        val x = pos.x
        val y = pos.y

        this.drawFullHexTile(x, y, size)
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

    private fun drawTerritory(entity: Entity) {
        val team = entity[TeamComponent.mapper]?.team ?: return
        if (team == TeamComponent.TeamName.NONE) return

        val pos = entity[PositionComponent.mapper] ?: return
        val x = pos.x.toFloat()
        val y = pos.y.toFloat()
        val size = 32f

        shapeRenderer.color = when (team) {
            TeamComponent.TeamName.RED -> Color(1f, 0f, 0f, 0.4f)  // 40% opacity red
            TeamComponent.TeamName.BLUE -> Color(0f, 0f, 1f, 0.4f) // 40% opacity blue
            else -> return
        }

        this.drawFullHexTile(x, y, size)
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

    private fun drawCity(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        batch.draw(cityTexture, pos.x - 8f, pos.y - 8f, 16f, 16f)
    }

    private fun drawTroop(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return

        val team = entity[TeamComponent.mapper] ?: return

        if (team.team == TeamComponent.TeamName.RED) {
            batch.draw(redTroopTexture, pos.x - 8f, pos.y - 8f, 16f, 16f)
        } else if (team.team == TeamComponent.TeamName.BLUE) {
            batch.draw(blueTroopTexture, pos.x - 8f, pos.y - 8f, 16f, 16f)
        }

    }

    private fun drawBackground() {
        batch.draw(backgroundTexture, -1f, -1f, 2f, 2f) // Using identity projection: drawing from (-1, -1) to (1, 1) fills the entire screen
    }

    override fun dispose() {
        cityTexture.disposeSafely()
        troopTexture.disposeSafely()
        backgroundTexture.disposeSafely()
        redTroopTexture.disposeSafely()
        blueTroopTexture.disposeSafely()
    }
}
