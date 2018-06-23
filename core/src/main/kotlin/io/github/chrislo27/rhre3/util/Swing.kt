package io.github.chrislo27.rhre3.util


/**
 * @param ratio 50..99
 * @param division 1.0 = 8th
 */
data class Swing(val ratio: Int, val division: Float) {

    companion object {
        val ABS_MIN_SWING: Int = 1
        val MIN_SWING: Int = 50
        val MAX_SWING: Int = 99

        val EIGHTH_DIVISION = 1.0f
        val SIXTEENTH_DIVISION = 0.5f

        val STRAIGHT: Swing = Swing(50, EIGHTH_DIVISION)
        val SWING: Swing = Swing(65, EIGHTH_DIVISION)
        val SHUFFLE: Swing = Swing(75, EIGHTH_DIVISION)
    }

}
