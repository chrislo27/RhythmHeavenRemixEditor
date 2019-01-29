package io.github.chrislo27.rhre3.modding

import java.util.*
import kotlin.math.roundToInt

enum class ModdingGame(val id: String, val gameName: String, val console: String,
                       val tickflowUnits: Int,
                       val tickflowUnitName: String = "",
                       val underdeveloped: Boolean = true) {
    TENGOKU("gba", "Rhythm Tengoku (リズム天国)", "GBA", 0x18),
    DS_NA("rhds", "Rhythm Heaven", "NDS", 0x0C),
    FEVER("rhFever", "Rhythm Heaven Fever", "Wii", 0x30),
    MEGAMIX_NA("rhMegamix", "Rhythm Heaven Megamix", "3DS", 0x30, underdeveloped = false); // τ

    companion object {
        val DEFAULT_GAME = MEGAMIX_NA
        val VALUES = values().toList()
    }

    private val tickflowUnitsStr: String = "0x${tickflowUnits.toString(16).toUpperCase(Locale.ROOT)}"
    val fullName: String = "$gameName ($console)"

    fun beatsToTickflow(beats: Float): Int = (beats * tickflowUnits).roundToInt()
    fun tickflowToBeats(tickflow: Int): Float = tickflow.toFloat() / tickflowUnits

    fun beatsToTickflowString(beats: Float): String {
        val tickflow = beatsToTickflow(beats)
        val wholes = tickflow / tickflowUnits
        val leftover = tickflow % tickflowUnits

        return "${if (wholes > 0) if (wholes > 1) "$wholes × $tickflowUnitsStr" else tickflowUnitsStr else ""}${if (wholes > 0 && leftover > 0) " + " else ""}${if (leftover > 0) "0x${leftover.toString(16).toUpperCase(Locale.ROOT)}" else ""} $tickflowUnitName".trim()
    }
}