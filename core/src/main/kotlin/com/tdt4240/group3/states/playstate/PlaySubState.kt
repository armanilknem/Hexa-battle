package com.tdt4240.group3.game.playstate

import com.tdt4240.group3.screens.PlayScreen

interface PlaySubState {
    val backgroundColor: Triple<Float, Float, Float>
        get() = Triple(0.1f, 0.35f, 0.1f) // default green
    fun enter(screen: PlayScreen) {}
    fun handleInput(screen: PlayScreen)
    fun update(screen: PlayScreen, delta: Float)
    fun render(screen: PlayScreen)
    fun exit(screen: PlayScreen) {}
}
