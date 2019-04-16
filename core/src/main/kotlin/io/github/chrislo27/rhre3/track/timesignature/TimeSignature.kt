package io.github.chrislo27.rhre3.track.timesignature


class TimeSignature(val container: TimeSignatures, val beat: Float, beatsPerMeasure: Int, beatUnit: Int) {

    companion object {
        val LOWER_BEATS_PER_MEASURE = 1
        val UPPER_BEATS_PER_MEASURE = 64
        val NOTE_UNITS = listOf(2, 4, 8, 16).sorted()
        val DEFAULT_NOTE_UNIT = 4
    }

    var beatsPerMeasure: Int = 4
        set(value) {
            field = value.coerceIn(LOWER_BEATS_PER_MEASURE, UPPER_BEATS_PER_MEASURE)
        }
    var beatUnit: Int = DEFAULT_NOTE_UNIT
        set(value) {
            field = if (value !in NOTE_UNITS) {
                DEFAULT_NOTE_UNIT
            } else {
                value
            }
        }
    val noteFraction: Float get() = 4f / beatUnit

    init {
        this.beatsPerMeasure = beatsPerMeasure
        this.beatUnit = beatUnit
    }

    var measure: Int = 0
    val lowerText: String = this.beatUnit.toString()
    val upperText: String = "${this.beatsPerMeasure}"

}
