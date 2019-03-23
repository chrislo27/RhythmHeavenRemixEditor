package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.controllers.mappings.Xbox
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName


data class ControllerMapping(var inUse: Boolean, val name: String,
                             var buttonA: ControllerInput = ControllerInput.None,
                             var buttonB: ControllerInput = ControllerInput.None,
                             var buttonLeft: ControllerInput = ControllerInput.None,
                             var buttonRight: ControllerInput = ControllerInput.None,
                             var buttonUp: ControllerInput = ControllerInput.None,
                             var buttonDown: ControllerInput = ControllerInput.None) {

    companion object {
        val INVALID = ControllerMapping(false, "<none>")
        val XBOX = ControllerMapping(false, "XBOX something",
                                     buttonA = ControllerInput.Button(Xbox.B), buttonB = ControllerInput.Button(Xbox.A),
                                     buttonLeft = if (Xbox.DPAD_LEFT == -1) ControllerInput.Pov(0,  PovDirection.west) else ControllerInput.Button(Xbox.DPAD_LEFT),
                                     buttonRight = if (Xbox.DPAD_RIGHT == -1) ControllerInput.Pov(0, PovDirection.east) else ControllerInput.Button(Xbox.DPAD_RIGHT),
                                     buttonUp = if (Xbox.DPAD_UP == -1) ControllerInput.Pov(0, PovDirection.north) else ControllerInput.Button(Xbox.DPAD_UP),
                                     buttonDown = if (Xbox.DPAD_DOWN == -1) ControllerInput.Pov(0, PovDirection.south) else ControllerInput.Button(Xbox.DPAD_DOWN))
    }

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(ControllerInput.None::class),
        JsonSubTypes.Type(ControllerInput.Button::class),
        JsonSubTypes.Type(ControllerInput.Pov::class)
             )
sealed class ControllerInput {
    @JsonTypeName("none")
    object None : ControllerInput() {
        override fun isNothing(): Boolean = true
        override fun toString(): String {
            return "<none>"
        }
    }
    @JsonTypeName("button")
    class Button(val code: Int) : ControllerInput() {
        override fun isNothing(): Boolean = code < 0
        override fun toString(): String {
            return "Button $code"
        }
    }
    @JsonTypeName("pov")
    class Pov(val povCode: Int, val direction: PovDirection) : ControllerInput() {
        override fun isNothing(): Boolean = povCode < 0 || direction == PovDirection.center
        override fun toString(): String {
            return "PoV $povCode $direction"
        }
    }
//    class Axis(val axisCode: Int, val range: ClosedRange<Float>) : ControllerInput() // Not implemented

    abstract fun isNothing(): Boolean
}