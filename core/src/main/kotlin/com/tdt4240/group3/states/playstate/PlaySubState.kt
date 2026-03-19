package com.tdt4240.group3.game.playstate

import com.tdt4240.group3.screens.PlayScreen

interface PlaySubState {
    fun enter(screen: PlayScreen) {}
    fun handleInput(screen: PlayScreen)
    fun update(screen: PlayScreen, delta: Float)
    fun render(screen: PlayScreen)
    fun exit(screen: PlayScreen) {}
}
