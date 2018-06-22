package io.github.chrislo27.rhre3.track.tracker

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme


abstract class Tracker<SELF : Tracker<SELF>>(val container: TrackerContainer<SELF>,
                                             var beat: Float, width: Float) {

    open val allowsResize: Boolean = true
    var width: Float = width
        set(value) {
            field = value.coerceAtLeast(0f)
        }
    val isZeroWidth: Boolean
        get() = width <= 0f
    val endBeat: Float
        get() = beat + width

    var text: String = ""
        protected set

    open fun getSlope(): Int = 0

    abstract fun getColour(theme: Theme): Color

    abstract fun createResizeCopy(beat: Float, width: Float): Tracker<SELF>

    open fun scroll(amount: Int, control: Boolean, shift: Boolean): Tracker<SELF>? = null

}