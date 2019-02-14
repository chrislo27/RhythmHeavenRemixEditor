package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.Input


data class PlayalongControls(var buttonA: Int = Input.Keys.J,
                             var buttonB: Int = Input.Keys.K,
                             var buttonLeft: Int = Input.Keys.A,
                             var buttonRight: Int = Input.Keys.D,
                             var buttonUp: Int = Input.Keys.W,
                             var buttonDown: Int = Input.Keys.S) {

    companion object {
        val QWERTY_D_PAD_LEFT: PlayalongControls = PlayalongControls()

    }

    fun toInputMap(): Map<PlayalongInput, Int> {
        return linkedMapOf(PlayalongInput.BUTTON_A to buttonA,
                     PlayalongInput.BUTTON_B to buttonB,
                     PlayalongInput.BUTTON_DPAD_UP to buttonUp,
                     PlayalongInput.BUTTON_DPAD_DOWN to buttonDown,
                     PlayalongInput.BUTTON_DPAD_LEFT to buttonLeft,
                     PlayalongInput.BUTTON_DPAD_RIGHT to buttonRight)
    }

    fun toInputString(): String {
        val inputMap = toInputMap()
        return inputMap.entries.joinToString(separator = "  [GRAY]|[]  ") { (k, v) -> "${k.longDisplayText} - ${Input.Keys.toString(v)}"}
    }

}