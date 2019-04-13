package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.Input
import java.util.*


data class PlayalongControls(var buttonA: Int = Input.Keys.J,
                             var buttonB: Int = Input.Keys.K,
                             var buttonLeft: Int = Input.Keys.A,
                             var buttonRight: Int = Input.Keys.D,
                             var buttonUp: Int = Input.Keys.W,
                             var buttonDown: Int = Input.Keys.S) {

    companion object {
        val QWERTY_WASD_JK = PlayalongControls()
        val AZERTY_ZSQD_JK = PlayalongControls(buttonLeft = Input.Keys.Q, buttonRight = Input.Keys.D, buttonUp = Input.Keys.Z, buttonDown = Input.Keys.S)
        val ARROW_KEYS_ZX = PlayalongControls(Input.Keys.Z, Input.Keys.X, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.DOWN)
        val ARROW_KEYS_WX = PlayalongControls(Input.Keys.W, Input.Keys.X, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.DOWN)
        val QWERTY_IJKL = PlayalongControls(Input.Keys.Z, Input.Keys.X, Input.Keys.J, Input.Keys.L, Input.Keys.I, Input.Keys.K)
        val AZERTY_IJKL = PlayalongControls(Input.Keys.W, Input.Keys.X, Input.Keys.J, Input.Keys.L, Input.Keys.I, Input.Keys.K)
        val QWERTY_WASD_JN = PlayalongControls(buttonB = Input.Keys.N)
        val AZERTY_ZSQD_JN = PlayalongControls(buttonB = Input.Keys.N, buttonLeft = Input.Keys.Q, buttonRight = Input.Keys.D, buttonUp = Input.Keys.Z, buttonDown = Input.Keys.S)
        val INVALID = PlayalongControls(-1, -1, -1, -1, -1, -1)

        val strCustom = "Custom"
        val standardControls: Map<String, PlayalongControls> = mapOf(
                "QWERTY WASD/JK" to QWERTY_WASD_JK,
                "QWERTY Arrow Keys/ZX" to ARROW_KEYS_ZX,
                "QWERTY IJKL/ZX" to QWERTY_IJKL,
                "QWERTY WASD/JN" to QWERTY_WASD_JN,

                "AZERTY ZSQD/JK" to AZERTY_ZSQD_JK,
                "AZERTY Arrow Keys/WX" to ARROW_KEYS_WX,
                "AZERTY IJKL/WX" to AZERTY_IJKL,
                "AZERTY ZSQD/JN" to AZERTY_ZSQD_JN
                                                                    )
    }

    fun toInputMap(): Map<PlayalongInput, Set<Int>> {
        return linkedMapOf(PlayalongInput.BUTTON_A to setOf(buttonA),
                           PlayalongInput.BUTTON_B to setOf(buttonB),
                           PlayalongInput.BUTTON_DPAD_UP to setOf(buttonUp),
                           PlayalongInput.BUTTON_DPAD_DOWN to setOf(buttonDown),
                           PlayalongInput.BUTTON_DPAD_LEFT to setOf(buttonLeft),
                           PlayalongInput.BUTTON_DPAD_RIGHT to setOf(buttonRight),
                           PlayalongInput.BUTTON_A_OR_DPAD to setOf(buttonA, buttonUp, buttonDown, buttonLeft, buttonRight),
                           PlayalongInput.BUTTON_DPAD to setOf(buttonUp, buttonDown, buttonLeft, buttonRight)
                          )
    }

    fun toInputString(selected: EnumSet<PlayalongInput>? = null): String {
        val inputMap = linkedMapOf(PlayalongInput.BUTTON_A to setOf(buttonA),
                                   PlayalongInput.BUTTON_B to setOf(buttonB),
                                   PlayalongInput.BUTTON_DPAD_UP to setOf(buttonUp),
                                   PlayalongInput.BUTTON_DPAD_DOWN to setOf(buttonDown),
                                   PlayalongInput.BUTTON_DPAD_LEFT to setOf(buttonLeft),
                                   PlayalongInput.BUTTON_DPAD_RIGHT to setOf(buttonRight)
                                  )
        return inputMap.entries.joinToString(separator = "  [GRAY]|[]  ") { (k, v) ->
            val isSelected = selected?.contains(k) == true
            "${if (isSelected) "[CYAN]" else ""}${k.longDisplayText}${if (isSelected) "[]" else ""} - ${v.joinToString(separator = ", ") { Input.Keys.toString(it) }}"
        }
    }

}