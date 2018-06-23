package io.github.chrislo27.rhre3.util


object TempoUtils {
    fun beatsToSeconds(beat: Float, bpm: Float): Float =
            beat / (bpm / 60)

    fun secondsToBeats(seconds: Float, bpm: Float): Float =
            seconds * (bpm / 60)
}

object SwingUtils {
    /**
     * @param ratio between 0.5 and 1.0 EXCLUSIVELY
     */
    private fun basicLinearToSwing(alpha: Float, ratio: Float): Float {
        return if (alpha <= ratio) {
            0.5f / ratio * alpha
        } else {
            0.5f + 0.5f / (1f - ratio) * (alpha - ratio)
        }
    }

    /**
     * @param ratio between 0.5 and 1.0 EXCLUSIVELY
     */
    private fun basicSwingToLinear(alpha: Float, ratio: Float): Float {
        return if (alpha <= 0.5f) {
            2 * ratio * alpha
        } else {
            2 * (alpha - 0.5f) * (1f - ratio) + ratio
        }
    }

    /**
     * Converts linear beats to swing beats.
     *
     * @param ratio between 0.5 and 1.0 EXCLUSIVELY
     * @param division The note division as a ratio of eighth notes. Usually 1.0 or 0.5 (8th and 16th respectively).
     */
    fun linearToSwing(linear: Float, ratio: Float, division: Float): Float {
        val base: Int = Math.floor(linear.toDouble() / division).toInt()

        return base * division + basicLinearToSwing(linear % division, ratio)
    }

    /**
     * Converts swing beats to linear beats.
     *
     * @param ratio between 0.5 and 1.0 EXCLUSIVELY
     * @param division The note division as a ratio of eighth notes. Usually 1.0 or 0.5 (8th and 16th respectively).
     */
    fun swingToLinear(swing: Float, ratio: Float, division: Float): Float {
        val base: Int = Math.floor(swing.toDouble() / division).toInt()

        return base * division + basicSwingToLinear(swing % division, ratio)
    }

    /**
     * Converts linear beats to swing beats.
     */
    fun linearToSwing(linear: Float, swing: Swing): Float {
        return linearToSwing(linear, swing.ratio / 100f, swing.division)
    }

    /**
     * Converts swing beats to linear beats.
     */
    fun swingToLinear(swung: Float, swing: Swing): Float {
        return swingToLinear(swung, swing.ratio / 100f, swing.division)
    }


}
