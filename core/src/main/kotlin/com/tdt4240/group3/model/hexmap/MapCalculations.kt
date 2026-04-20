package com.tdt4240.group3.model.hexmap

import kotlin.math.abs

object MapCalculations {

    fun hexDistance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        return (abs(q1 - q2) + abs(q1 + r1 - q2 - r2) + abs(r1 - r2)) / 2
    }
}
