package io.github.chrislo27.rhre3.util


object BpmUtils {
    fun beatsToSeconds(beat: Float, bpm: Float): Float =
            beat / (bpm / 60)

    fun secondsToBeats(seconds: Float, bpm: Float): Float =
            seconds * (bpm / 60)
}

object Semitones {

    const val SEMITONES_IN_OCTAVE = 12
    const val SEMITONE_VALUE = 1f / SEMITONES_IN_OCTAVE
    const val SHARP = "♯"

    private val cachedPitches = mutableMapOf<Int, Float>()
    private val keyNames = mutableMapOf(
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
            11 to "B"
                                       )
    private val usedKeyNames = mutableMapOf<Int, String>().apply {
        putAll(keyNames)
    }

    @JvmStatic
    fun getSemitoneName(semitone: Int): String {
        val abs = Math.abs(Math.floorMod(semitone, SEMITONES_IN_OCTAVE))
        if (!usedKeyNames.containsKey(semitone)) {
            val repeats = Math.abs(Math.floorDiv(semitone, SEMITONES_IN_OCTAVE))
            usedKeyNames[semitone] = keyNames[abs]!! +
                    if ((semitone >= 12 || semitone <= -1))
                        (if (repeats > 1) "$repeats" else "") + (if (semitone > 0) "+" else "-")
                    else ""
        }

        return usedKeyNames[semitone]!!
    }

    @JvmStatic
    fun getALPitch(semitone: Int): Float {
        if (cachedPitches[semitone] == null) {
            cachedPitches.put(semitone, Math.pow(2.0, (semitone * SEMITONE_VALUE).toDouble()).toFloat())
        }

        return cachedPitches[semitone] ?: 1f
    }

}