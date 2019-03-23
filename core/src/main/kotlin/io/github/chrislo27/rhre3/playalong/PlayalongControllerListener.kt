package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.controllers.mappings.Xbox
import com.badlogic.gdx.math.Vector3
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.Toolboks
import java.util.*


class PlayalongControllerListener(val playalongGetter: () -> Playalong) : ControllerListener {

    private fun getMapping(controller: Controller): ControllerMapping? = Playalong.activeControllerMappings[controller]

    private val playalong: Playalong get() = playalongGetter()

    override fun connected(controller: Controller) {
        Toolboks.LOGGER.info("[PlayalongControllerListener] Controller ${controller.name} connected")
    }

    override fun disconnected(controller: Controller) {
        Toolboks.LOGGER.info("[PlayalongControllerListener] Controller ${controller.name} disconnected")
    }

    private fun ControllerMapping.containsAnyButton(buttonCode: Int): Boolean {
        val buttonA = buttonA
        val buttonB = buttonB
        val buttonLeft = buttonLeft
        val buttonRight = buttonRight
        val buttonUp = buttonUp
        val buttonDown = buttonDown
        return (buttonA is ControllerInput.Button && buttonA.code == buttonCode) ||
                (buttonB is ControllerInput.Button && buttonB.code == buttonCode) ||
                (buttonUp is ControllerInput.Button && buttonUp.code == buttonCode) ||
                (buttonDown is ControllerInput.Button && buttonDown.code == buttonCode) ||
                (buttonLeft is ControllerInput.Button && buttonLeft.code == buttonCode) ||
                (buttonRight is ControllerInput.Button && buttonRight.code == buttonCode)
    }

    override fun buttonDown(controller: Controller, buttonCode: Int): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown
        val anyStart = mapping.containsAnyButton(Xbox.START)
        val anyBack = mapping.containsAnyButton(Xbox.BACK)
        var any = false
        if (buttonA is ControllerInput.Button && buttonA.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_A, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonB is ControllerInput.Button && buttonB.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_B), buttonCode shl 16, false)
            any = true
        }
        if (buttonLeft is ControllerInput.Button && buttonLeft.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_DPAD_LEFT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonRight is ControllerInput.Button && buttonRight.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_DPAD_RIGHT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonUp is ControllerInput.Button && buttonUp.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_DPAD_UP, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonDown is ControllerInput.Button && buttonDown.code == buttonCode) {
            playalong.handleInput(true, EnumSet.of(PlayalongInput.BUTTON_DPAD_DOWN, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (Xbox.isXboxController(controller) && buttonCode == Xbox.START && !anyStart) {
            // Pause/Play
            playalong.remix.playState = if (playalong.remix.playState != PlayState.PLAYING) PlayState.PLAYING else PlayState.PAUSED
            return true
        }
        if (Xbox.isXboxController(controller) && buttonCode == Xbox.BACK && !anyBack) {
            // Stop
            playalong.remix.playState = PlayState.STOPPED
            return true
        }
        return any && playalong.remix.playState == PlayState.PLAYING
    }

    override fun buttonUp(controller: Controller, buttonCode: Int): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown
        var any = false
        if (buttonA is ControllerInput.Button && buttonA.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_A, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonB is ControllerInput.Button && buttonB.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_B), buttonCode shl 16, false)
            any = true
        }
        if (buttonLeft is ControllerInput.Button && buttonLeft.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_DPAD_LEFT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonRight is ControllerInput.Button && buttonRight.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_DPAD_RIGHT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonUp is ControllerInput.Button && buttonUp.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_DPAD_UP, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        if (buttonDown is ControllerInput.Button && buttonDown.code == buttonCode) {
            playalong.handleInput(false, EnumSet.of(PlayalongInput.BUTTON_DPAD_DOWN, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonCode shl 16, false)
            any = true
        }
        return any && playalong.remix.playState == PlayState.PLAYING
    }

    override fun povMoved(controller: Controller, povCode: Int, value: PovDirection): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown
        var any = false
        val release = value == PovDirection.center
        if (buttonA is ControllerInput.Pov && buttonA.povCode == povCode && (buttonA.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_A, PlayalongInput.BUTTON_A_OR_DPAD), buttonA.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        if (buttonB is ControllerInput.Pov && buttonB.povCode == povCode && (buttonB.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_B), buttonB.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        if (buttonLeft is ControllerInput.Pov && buttonLeft.povCode == povCode && (buttonLeft.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_DPAD_LEFT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonLeft.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        if (buttonRight is ControllerInput.Pov && buttonRight.povCode == povCode && (buttonRight.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_DPAD_RIGHT, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonRight.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        if (buttonUp is ControllerInput.Pov && buttonUp.povCode == povCode && (buttonUp.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_DPAD_UP, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonUp.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        if (buttonDown is ControllerInput.Pov && buttonDown.povCode == povCode && (buttonDown.direction == value || release)) {
            playalong.handleInput(!release, EnumSet.of(PlayalongInput.BUTTON_DPAD_DOWN, PlayalongInput.BUTTON_DPAD, PlayalongInput.BUTTON_A_OR_DPAD), buttonDown.direction.ordinal shl (povCode + 16), false)
            any = true
        }
        return any && playalong.remix.playState == PlayState.PLAYING
    }

    // Below not implemented
    override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean = false

    override fun accelerometerMoved(controller: Controller, accelerometerCode: Int, value: Vector3): Boolean = false

    override fun xSliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false

    override fun ySliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false


}