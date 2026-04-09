package com.tdt4240.group3.model.entities

import com.badlogic.ashley.core.Engine
import com.tdt4240.group3.model.components.*
import com.tdt4240.group3.model.entities.EntityFactory
import ktx.ashley.get
import ktx.ashley.mapperFor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EntityFactoryTest {

    private lateinit var engine: Engine
    private lateinit var factory: EntityFactory

    @Before
    fun setup() {
        engine = Engine()
        factory = EntityFactory(engine)
    }

    @Test
    fun `createTile adds entity with correct components`() {
        val tile = factory.createTile(1, 2, TileComponent.TileType.GRASS)

        val tileComp = tile[TileComponent.mapper]
        val pos = tile[PositionComponent.mapper]

        assertNotNull(pos)
        assertNotNull(tileComp)

        assertEquals(1, pos!!.q)
        assertEquals(2, pos.r)
        assertEquals(TileComponent.TileType.GRASS, tileComp!!.type)
        println("createTile: PositionComponent(q=${pos.q}, r=${pos.r}), TileType=${tileComp.type}")
    }

    @Test
    fun `generateRectangularGrid creates correct number of tiles`() {
        factory.generateRectangularGrid(3, 3)

        // A 3x3 grid should have 9 entities
        assertEquals(9, engine.entities.size())
        println("generateRectangularGrid: created ${engine.entities.size()} tiles for a 3x3 grid")
    }

    @Test
    fun `createTile pixel position is non-zero for non-origin hex`() {
        val tile = factory.createTile(1, 1, TileComponent.TileType.GRASS)
        val pos = tile[PositionComponent.mapper]

        assertNotNull(pos)
        val x = pos!!.x
        val y = pos.y
        assertTrue(x != 0f || y != 0f)
        println("createTile pixel position: Position(x=$x, y=$y)")
    }
}
