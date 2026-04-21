package com.tdt4240.group3.controller

import com.tdt4240.group3.view.states.PauseState
import com.tdt4240.group3.view.states.PlaySubState

object PauseController {
    fun togglePause(currentState: PlaySubState, previousState: PlaySubState): PlaySubState =
        if (currentState is PauseState) previousState else PauseState()
}
