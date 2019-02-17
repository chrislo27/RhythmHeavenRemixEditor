package io.github.chrislo27.rhre3.playalong

import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_A
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_B
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_D
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_L
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_R
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_DPAD_U
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_JOY_D
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_JOY_L
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_JOY_R
import io.github.chrislo27.rhre3.playalong.PlayalongChars.FILLED_JOY_U


enum class PlayalongInput(val id: String, val displayText: String,
                          val longDisplayText: String = displayText,
                          val trackDisplayText: String = displayText,
                          val deprecatedIDs: List<String> = listOf(),
                          val isTouchScreen: Boolean = false) {

    BUTTON_A("A", FILLED_A),
    BUTTON_B("B", FILLED_B),
    BUTTON_DPAD("+", FILLED_DPAD),
    BUTTON_A_OR_DPAD("A_+", "$FILLED_A/$FILLED_DPAD", trackDisplayText = "$FILLED_A/$FILLED_DPAD"),
    BUTTON_DPAD_UP("+_up", FILLED_DPAD_U, "$FILLED_DPAD $FILLED_JOY_U"),
    BUTTON_DPAD_DOWN("+_down", FILLED_DPAD_D, "$FILLED_DPAD $FILLED_JOY_D"),
    BUTTON_DPAD_LEFT("+_left", FILLED_DPAD_L, "$FILLED_DPAD $FILLED_JOY_L"),
    BUTTON_DPAD_RIGHT("+_right", FILLED_DPAD_R, "$FILLED_DPAD $FILLED_JOY_R"),

    TOUCH_TAP("touch_tap", "Tap", isTouchScreen = true),
    TOUCH_FLICK("touch_flick", "Flick", isTouchScreen = true),
    TOUCH_RELEASE("touch_release", "Release", isTouchScreen = true),
    TOUCH_QUICK_TAP("touch_quick_tap", "Quick Tap", trackDisplayText = "Quick\nTap", isTouchScreen = true),
    TOUCH_SLIDE("touch_slide", "Slide", isTouchScreen = true);

    companion object {
        val VALUES: List<PlayalongInput> = values().toList()
        private val ID_MAP: Map<String, PlayalongInput> = VALUES.flatMap { pi -> listOf(pi.id to pi) + pi.deprecatedIDs.map { i -> i to pi } }.toMap()
        private val INDICES_MAP: Map<PlayalongInput, Int> = VALUES.associateWith(VALUES::indexOf)
        private val REVERSE_INDICES_MAP: Map<PlayalongInput, Int> = VALUES.associateWith { VALUES.size - 1 - VALUES.indexOf(it) }
        val NUMBER_RANGE: IntRange = 0 until VALUES.size

        operator fun get(id: String): PlayalongInput? = ID_MAP[id]

        fun indexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else INDICES_MAP.getOrDefault(playalongInput, -1)
        fun reverseIndexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else REVERSE_INDICES_MAP.getOrDefault(playalongInput, -1)
    }
}