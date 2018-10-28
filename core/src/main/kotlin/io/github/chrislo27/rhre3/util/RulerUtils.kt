package io.github.chrislo27.rhre3.util

import kotlin.math.roundToInt


object RulerUtils {

    fun widthToMixedNumber(beats: Float, snap: Float): String {
        if (snap <= 0f) return "$beats"
        val denominator = (1.0 / snap.toDouble()).roundToInt()
        val improperNumerator = (beats * denominator).roundToInt()
        val numerator = improperNumerator % denominator
        val whole = improperNumerator / denominator

        return if (numerator > 0) {
            if (whole > 0) {
                "$whole $numerator/$denominator"
            } else {
                "$numerator/$denominator"
            }
        } else {
            "$whole"
        }
    }

}