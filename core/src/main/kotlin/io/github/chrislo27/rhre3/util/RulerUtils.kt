package io.github.chrislo27.rhre3.util

import kotlin.math.roundToInt


object RulerUtils {

    fun widthToMixedNumber(beats: Float, snap: Float): String {
        if (snap <= 0f) return "$beats"
        val whole = Math.floor(beats.toDouble()).roundToInt()
        val decimalPart = (beats - whole).coerceAtLeast(0f)
        val denominator = (1.0 / snap.toDouble()).roundToInt()
        val numerator = (decimalPart * denominator).roundToInt()

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