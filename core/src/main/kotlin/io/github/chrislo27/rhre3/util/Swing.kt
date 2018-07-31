package io.github.chrislo27.rhre3.util

import kotlin.math.roundToInt


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
        val LIGHT_SWING: Swing = Swing(60, EIGHTH_DIVISION)
        val MEDIUM_SWING: Swing = Swing(67, EIGHTH_DIVISION)
        val HARD_SWING: Swing = Swing(75, EIGHTH_DIVISION)

        val SWING_NAMES: Map<Swing, String> = mapOf(STRAIGHT to "Straight", LIGHT_SWING to "Light Swing", MEDIUM_SWING to "Swing", HARD_SWING to "Hard Swing")
        val SWING_LIST: List<Swing> = SWING_NAMES.keys.toList()

        fun getSwingNameFromRatio(ratio: Int): String {
            return SWING_NAMES.entries.lastOrNull { entry -> entry.key.ratio <= ratio }?.let { entry ->
                if (entry.key == STRAIGHT && ratio != entry.key.ratio) {
                    SWING_NAMES[Swing.LIGHT_SWING]
                } else entry.value
            } ?: "Inverted Swing"
        }

        fun getNoteSymbolFromNoteType(noteType: Int): String {
            return when (noteType) {
                8 -> Swing.EIGHTH_SYMBOL
                16 -> Swing.SIXTEENTH_SYMBOL
                else -> "${noteType}th"
            }
        }
    }

    fun getSwingName(): String = getSwingNameFromRatio(ratio)

    fun getNoteSymbol(): String = getNoteSymbolFromNoteType((8f / division).roundToInt())

}
