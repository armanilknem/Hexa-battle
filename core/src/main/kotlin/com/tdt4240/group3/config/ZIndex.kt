package com.tdt4240.group3.config

/**
 * Rendering layer order for entities drawn in the same batch pass.
 * Higher values render on top of lower values.
 *
 * Note: the ordering between different draw passes (tiles → cities → troops)
 * is enforced by the explicit call order in [com.tdt4240.group3.view.View].
 * These constants control ordering *within* each pass when multiple entities
 * of the same type share a tile (e.g. a capital rendered above a normal city).
 */
object ZIndex {
    const val TILE  = 0
    const val CITY  = 1
    const val TROOP = 2
}
