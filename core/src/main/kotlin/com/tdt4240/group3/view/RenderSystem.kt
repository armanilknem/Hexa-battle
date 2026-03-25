package com.tdt4240.group3.view.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Disposable
import com.tdt4240.group3.model.components.CityComponent
import com.tdt4240.group3.model.components.PositionComponent
import com.tdt4240.group3.model.components.TileComponent
import com.tdt4240.group3.model.components.TroopComponent // add when ready
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.assets.disposeSafely
import ktx.graphics.use
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RenderSystem(
    private val batch: SpriteBatch,
    private val shapeRenderer: ShapeRenderer,
    private val camera: OrthographicCamera
) : EntitySystem(), Disposable {

    // Three distinct families — one per entity type
    private val tileFamily  = allOf(PositionComponent::class, TileComponent::class).get()
    private val cityFamily  = allOf(PositionComponent::class, CityComponent::class).get()
    private val troopFamily = allOf(PositionComponent::class, TroopComponent::class).get()

    private val cityTexture  = Texture(Gdx.files.internal("Manchester_City_FC_badge.svg.png"))
    private val troopTexture = Texture(Gdx.files.internal("troop.png"))

    override fun update(deltaTime: Float) {
        val entities = engine.entities

        shapeRenderer.projectionMatrix = camera.combined

        // Pass 1a — filled highlights
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled) {
            entities.forEach { entity ->
                if (tileFamily.matches(entity)) drawTileHighlight(entity)
            }
        }

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
        shapeRenderer.color = Color(1f, 1f, 1f, 0.25f)
        shapeRenderer.circle(pos.x.toFloat(), pos.y.toFloat(), 28f, 6)
    }

    private fun drawTile(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        val x = pos.x.toFloat()
        val y = pos.y.toFloat()
        val size = 32f
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
        batch.draw(cityTexture, pos.x - 16f, pos.y - 16f, 32f, 32f)
    }

    private fun drawTroop(entity: Entity) {
        val pos = entity[PositionComponent.mapper] ?: return
        batch.draw(troopTexture, pos.x - 16f, pos.y - 16f, 32f, 32f)
    }

    override fun dispose() {
        cityTexture.disposeSafely()
        troopTexture.disposeSafely()  // was missing before
    }
}
