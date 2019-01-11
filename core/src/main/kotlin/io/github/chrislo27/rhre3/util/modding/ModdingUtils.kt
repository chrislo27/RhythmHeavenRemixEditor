package io.github.chrislo27.rhre3.util.modding

import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.i18n.Localization
import java.util.*
import kotlin.math.roundToInt


object ModdingUtils {

    enum class Game(val id: String, val localization: String, val console: String,
                    val tickflowUnits: Int,
                    val tickflowUnitName: String = "",
                    val incomplete: Boolean = true) {
        TENGOKU("gba", "series.tengoku.name", "GBA", 1),
        DS("ds", "series.ds.name", "NDS", 1),
        FEVER("fever", "series.fever.name", "Wii", 1),
        MEGAMIX("megamix", "series.megamix.name", "3DS", 0x30, incomplete = false); // τ

        companion object {
            val DEFAULT_GAME = MEGAMIX
            val VALUES = values().toList()
        }

        private val tickflowUnitsStr: String = "0x${tickflowUnits.toString(16).toUpperCase(Locale.ROOT)}"
        val localizedName: String = Localization[localization] + " ($console)"

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
    var currentGame: Game = Game.DEFAULT_GAME

}
