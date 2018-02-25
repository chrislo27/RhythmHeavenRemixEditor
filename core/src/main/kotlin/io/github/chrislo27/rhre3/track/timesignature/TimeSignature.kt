package io.github.chrislo27.rhre3.track.timesignature


class TimeSignature(val container: TimeSignatures, val beat: Int, divisions: Int) {

    companion object {
        private val FOUR = "4"
        val LOWER_LIMIT = 1
        val UPPER_LIMIT = 64
    }

    var divisions: Int = 4
        set(value) {
            field = value.coerceIn(LOWER_LIMIT, UPPER_LIMIT)
        }

    init {
        this.divisions = divisions
    }

    var measure: Int = 0
    val lowerText: String = FOUR
    val upperText: String = "${this.divisions}"

}
