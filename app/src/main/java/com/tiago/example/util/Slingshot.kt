package com.tiago.example.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object Slingshot {

    fun calculateTargetY(
        pointerY: Float,
        initialMotionY: Int,
        totalDragDistance: Int,
        dragRate: Float
    ): Int {
        val yDiff = pointerY - initialMotionY
        val scrollTop = yDiff * dragRate
        val boundedDragPercent = min(1f, abs(scrollTop / totalDragDistance))
        val extraOS = abs(scrollTop) - totalDragDistance

        val slingshotDistance = totalDragDistance.toFloat()
        val tensionSlingshotPercent = max(0f, min(extraOS, slingshotDistance * 2) / slingshotDistance)
        val tensionPercent = ((tensionSlingshotPercent / 4) - (tensionSlingshotPercent / 4).pow(2)) * 2f

        val extraMove = slingshotDistance * (tensionPercent / 2)

        return (slingshotDistance * boundedDragPercent + extraMove).toInt()
    }
}
