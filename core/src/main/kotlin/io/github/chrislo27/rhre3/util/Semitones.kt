package io.github.chrislo27.rhre3.util

import kotlin.math.abs
import kotlin.math.pow

object Semitones {

    const val SEMITONES_IN_OCTAVE = 12
    const val SEMITONE_VALUE = 1f / SEMITONES_IN_OCTAVE
    const val SHARP = "♯"
    const val FLAT = "♭"

    private val sharpKeyNames = mutableMapOf(
            0 to "C",
            1 to "C$SHARP",
            2 to "D",
            3 to "D$SHARP",
            4 to "E",
            5 to "F",
            6 to "F$SHARP",
            7 to "G",
            8 to "G$SHARP",
            9 to "A",
            10 to "A$SHARP",
            11 to "B")
    private val flatKeyNames = mutableMapOf(
            0 to "C",
            1 to "D$FLAT",
            2 to "D",
            3 to "E$FLAT",
            4 to "E",
            5 to "F",
            6 to "G$FLAT",
            7 to "G",
            8 to "A$FLAT",
            9 to "A",
            10 to "B$FLAT",
            11 to "B")

    enum class PitchStyle(val converter: (Int) -> String, val example: String) {
        SHARPS({ convertToName(sharpKeyNames, it)}, "A$SHARP"),
        FLATS({ convertToName(flatKeyNames, it) }, "B$FLAT"),
        INTEGRAL({ if (it == 0) "0" else if (it < 0) "$it" else "+$it" }, "+3, -5");

        val usedKeyNames = mutableMapOf<Int, String>()

        fun getSemitoneName(semitone: Int): String {
            return usedKeyNames.getOrPut(semitone) { converter(semitone) }
        }

        companion object {
            val VALUES = values().toList()

            private fun convertToName(baseMap: Map<Int, String>, semitone: Int): String {
                val abs = abs(Math.floorMod(semitone, SEMITONES_IN_OCTAVE))
                val repeats = kotlin.math.abs(Math.floorDiv(semitone, SEMITONES_IN_OCTAVE))
                return baseMap[abs] +
                        if ((semitone >= 12 || semitone <= -1))
                            (if (repeats > 1) "$repeats" else "") + (if (semitone > 0) "+" else "-")
                        else ""
            }
        }
    }


    private val cachedPitches = mutableMapOf<Int, Float>()
    var pitchStyle: PitchStyle = Semitones.PitchStyle.SHARPS

    fun getSemitoneName(semitone: Int): String {
        return pitchStyle.getSemitoneName(semitone)
    }

    fun getALPitch(semitone: Int): Float {
        return cachedPitches.getOrPut(semitone) { 2.0.pow((semitone * SEMITONE_VALUE).toDouble()).toFloat() }
    }

}