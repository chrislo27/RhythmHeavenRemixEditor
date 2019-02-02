package io.github.chrislo27.rhre3.playalong

import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_A
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_B
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_D
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_R


enum class PlayalongInput(val id: String, val displayText: String,
                          val deprecatedIDs: List<String> = listOf()) {

    BUTTON_A("button_A", FILLED_A),
    BUTTON_B("button_B", FILLED_B),
    BUTTON_DPAD("button_+", FILLED_DPAD),
    BUTTON_A_DPAD("button_A_+", "$FILLED_A/$FILLED_DPAD"),
    BUTTON_DPAD_DOWN("button_+_down", FILLED_DPAD_D),
    BUTTON_DPAD_RIGHT("button_+_right", FILLED_DPAD_R);

    companion object {
        val VALUES: List<PlayalongInput> = values().toList()
        private val ID_MAP: Map<String, PlayalongInput> = VALUES.flatMap { pi -> listOf(pi.id to pi) + pi.deprecatedIDs.map { i -> i to pi } }.toMap()
        private val INDICES_MAP: Map<PlayalongInput, Int> = VALUES.associateWith(VALUES::indexOf)
        val NUMBER_RANGE: IntRange = 0 until VALUES.size

        operator fun get(id: String): PlayalongInput? = ID_MAP[id]

        fun indexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else INDICES_MAP.getOrDefault(playalongInput, -1)
    }
}