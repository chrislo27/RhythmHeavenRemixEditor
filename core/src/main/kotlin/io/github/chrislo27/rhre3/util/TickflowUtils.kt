package io.github.chrislo27.rhre3.util

import java.util.*
import kotlin.math.roundToInt


object TickflowUtils {

    const val TICKFLOW_UNITS: Int = 0x48
    val TICKFLOW_UNITS_STR: String = "0x${TICKFLOW_UNITS.toString(16).toUpperCase(Locale.ROOT)}"

    fun beatsToTickflow(beats: Float): Int = (beats * TICKFLOW_UNITS).roundToInt()
    fun tickflowToBeats(tickflow: Int): Float = tickflow.toFloat() / TICKFLOW_UNITS

    fun beatsToTickflowString(beats: Float): String {
        val tickflow = beatsToTickflow(beats)
        val wholes = tickflow / TICKFLOW_UNITS
        val leftover = tickflow % TICKFLOW_UNITS

        return "${if (wholes > 0) if (wholes > 1) "$wholes * $TICKFLOW_UNITS_STR" else TICKFLOW_UNITS_STR else ""}${if (wholes > 0 && leftover > 0) " + " else ""}${if (leftover > 0) "0x${leftover.toString(16).toUpperCase(Locale.ROOT)}" else ""}"
    }

}