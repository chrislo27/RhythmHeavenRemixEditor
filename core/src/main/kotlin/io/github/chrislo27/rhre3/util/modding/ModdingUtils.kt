package io.github.chrislo27.rhre3.util.modding

import io.github.chrislo27.rhre3.RHRE3Application
import java.util.*
import kotlin.math.roundToInt


object ModdingUtils {

    enum class Game(val id: String, val localization: String, val tickflowUnits: Int, val tickflowUnitName: String = "") {
        TENGOKU("gba", "series.tengoku.name", 1),
        DS("ds", "series.ds.name", 1),
        FEVER("fever", "series.fever.name", 1),
        MEGAMIX("megamix", "series.megamix.name", 0x30); // τ

        companion object {
            val DEFAULT_GAME = MEGAMIX
            val VALUES = values().toList()
        }
        
        private val tickflowUnitsStr: String = "0x${tickflowUnits.toString(16).toUpperCase(Locale.ROOT)}"

        fun beatsToTickflow(beats: Float): Int = (beats * tickflowUnits).roundToInt()
        fun tickflowToBeats(tickflow: Int): Float = tickflow.toFloat() / tickflowUnits

        fun beatsToTickflowString(beats: Float): String {
            val tickflow = beatsToTickflow(beats)
            val wholes = tickflow / tickflowUnits
            val leftover = tickflow % tickflowUnits

            return "${if (wholes > 0) if (wholes > 1) "$wholes × $tickflowUnitsStr" else tickflowUnitsStr else ""}${if (wholes > 0 && leftover > 0) " + " else ""}${if (leftover > 0) "0x${leftover.toString(16).toUpperCase(Locale.ROOT)}" else ""} $tickflowUnitName".trim()
        }
    }

    val moddingToolsEnabled: Boolean get() = RHRE3Application.instance.advancedOptions
    var currentGame: Game = Game.MEGAMIX

}
