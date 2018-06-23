package io.github.chrislo27.rhre3.track.tracker.tempo

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.util.Swing
import io.github.chrislo27.rhre3.util.SwingUtils
import io.github.chrislo27.rhre3.util.TempoUtils


class TempoChange(container: TempoChanges, beat: Float, val bpm: Float, val swing: Swing)
    : Tracker<TempoChange>(container, beat, 0.0f) {

    companion object {
        val MIN_TEMPO: Float = 1.0f
        val MAX_TEMPO: Float = 600f
    }

    override val allowsResize: Boolean = false
    var seconds: Float = 0f

    init {
        text = "♩=${Editor.ONE_DECIMAL_PLACE_FORMATTER.format(bpm)}"
    }

    override fun scroll(amount: Int, control: Boolean, shift: Boolean): TempoChange? {
        val change = amount * (if (control) 5 else 1) * (if (shift) 0.1f else 1f)

        if ((change < 0 && bpm <= MIN_TEMPO) || (change > 0 && bpm >= MAX_TEMPO))
            return null

        return TempoChange(container as TempoChanges, beat, (bpm + change).coerceIn(MIN_TEMPO, MAX_TEMPO), swing)
    }

    fun scrollSwing(amount: Int, control: Boolean, shift: Boolean): TempoChange? {
        if (shift && !control) {
            return TempoChange(container as TempoChanges, beat, bpm, swing.copy(division = if (swing.division == Swing.EIGHTH_DIVISION) Swing.SIXTEENTH_DIVISION else Swing.EIGHTH_DIVISION))
        } else if (control) {
            val change = amount * (if (shift) 5 else 1)
            if ((change < 0 && swing.ratio > Swing.MIN_SWING) || (change > 0 && swing.ratio < Swing.MAX_SWING)) {
                return TempoChange(container as TempoChanges, beat, bpm, swing.copy(ratio = (swing.ratio + change).coerceIn(Swing.MIN_SWING, Swing.MAX_SWING)))
            }
        } else {
            val list = Swing.SWING_LIST
            val currentIndex: Int = if (swing.ratio < list.first().ratio) -1 else list.let { _ ->
                var last: Int = 0

                for (it in 0 until list.size) {
                    if (this.swing.ratio > list[last].ratio)
                        last = it
                }

                last
            }

            val nextIndex: Int = if (currentIndex == -1) {
                0
            } else {
                val futureNext = currentIndex + amount
                if (futureNext < 0)
                    list.size - 1
                else if (futureNext >= list.size)
                    0
                else
                    futureNext
            }

            if (nextIndex != currentIndex) {
                return TempoChange(container as TempoChanges, beat, bpm, swing.copy(ratio = list[nextIndex].ratio))
            }
        }

        return null
    }

    override fun createResizeCopy(beat: Float, width: Float): TempoChange {
        // Legacy, tempo changes cannot be resized anymore
        return TempoChange(container as TempoChanges, beat, bpm, swing)
    }

    override fun getColour(theme: Theme): Color {
        return theme.trackers.tempoChange
    }

    fun secondsToBeats(seconds: Float): Float {
        return endBeat + SwingUtils.linearToSwing(TempoUtils.secondsToBeats(seconds - this.seconds, tempoAtSeconds(seconds)), swing)
    }

    fun beatsToSeconds(beat: Float): Float {
        return seconds + TempoUtils.beatsToSeconds(SwingUtils.swingToLinear(beat - this.endBeat, swing), tempoAt(beat))
    }

    // Remnants of stretchable tempo changes

    fun tempoAt(beat: Float): Float {
        return bpm
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return bpm
    }

}