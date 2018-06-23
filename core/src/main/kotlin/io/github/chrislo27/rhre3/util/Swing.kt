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
        val EIGHTH_SYMBOL = "♪"
        val SIXTEENTH_SYMBOL = "♬"

        val STRAIGHT: Swing = Swing(50, EIGHTH_DIVISION)
        val SWING: Swing = Swing(60, EIGHTH_DIVISION)
        val SHUFFLE: Swing = Swing(70, EIGHTH_DIVISION)

        val SWING_NAMES: Map<Swing, String> = mapOf(STRAIGHT to "Straight", SWING to "Swing", SHUFFLE to "Shuffle")
        val SWING_LIST: List<Swing> = SWING_NAMES.keys.toList()
    }

}
