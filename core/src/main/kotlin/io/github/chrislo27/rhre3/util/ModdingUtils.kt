package io.github.chrislo27.rhre3.util

import io.github.chrislo27.rhre3.RHRE3Application
import java.util.*
import kotlin.math.roundToInt


object ModdingUtils {

    enum class Game(val localization: String, val tickflowUnits: Int, val tickflowUnitName: String = "") {
        TENGOKU("series.tengoku.name", 1),
        DS("series.ds.name", 1),
        FEVER("series.fever.name", 1),
        MEGAMIX("series.megamix.name", 0x30); // τ
        
        private val tickflowUnitsStr: String ="0x${tickflowUnits.toString(16).toUpperCase(Locale.ROOT)}"

        fun beatsToTickflow(beats: Float): Int = (beats * tickflowUnits).roundToInt()
        fun tickflowToBeats(tickflow: Int): Float = tickflow.toFloat() / tickflowUnits

        fun beatsToTickflowString(beats: Float): String {
            val tickflow = beatsToTickflow(beats)
            val wholes = tickflow / tickflowUnits
            val leftover = tickflow % tickflowUnits

            return "${if (wholes > 0) if (wholes > 1) "$wholes * $tickflowUnitsStr" else tickflowUnitsStr else ""}${if (wholes > 0 && leftover > 0) " + " else ""}${if (leftover > 0) "0x${leftover.toString(16).toUpperCase(Locale.ROOT)}" else ""} $tickflowUnitName".trim()
        }
    }

    var moddingToolsEnabled: Boolean = false
        get() = RHRE3Application.instance.advancedOptions // FIXME temporary fix
    var currentGame: Game = Game.MEGAMIX

}
