package com.tiago.example.util

import android.view.ViewGroup
import android.view.animation.Animation
import kotlin.math.roundToInt

fun Animation.onAnimationEnd(onEnd: () -> Unit) {
    setAnimationListener(
        object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                onEnd()
            }
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        }
    )
}

fun ViewGroup.dpToPx(dp: Int): Int {
    val density = context.resources.displayMetrics.density
    return (dp.toFloat() * density).roundToInt()
}
