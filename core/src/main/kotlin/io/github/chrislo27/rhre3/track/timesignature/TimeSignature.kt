package io.github.chrislo27.rhre3.track.timesignature


class TimeSignature(val container: TimeSignatures, val beat: Int, divisions: Int) {

    companion object {
        val FOUR = "4"
        val LOWER_BEATS_PER_MEASURE = 1
        val UPPER_BEATS_PER_MEASURE = 64
    }

    var divisions: Int = 4
        set(value) {
            field = value.coerceIn(LOWER_BEATS_PER_MEASURE, UPPER_BEATS_PER_MEASURE)
        }

    init {
        this.divisions = divisions
    }

    var measure: Int = 0
    val lowerText: String = FOUR
    val upperText: String = "${this.divisions}"

}
