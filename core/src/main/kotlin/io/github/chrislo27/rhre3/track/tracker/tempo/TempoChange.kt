package io.github.chrislo27.rhre3.track.tracker.tempo

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.util.BpmUtils


class TempoChange(container: TempoChanges, beat: Float,  val bpm: Float)
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

        return TempoChange(container as TempoChanges, beat, (bpm + change).coerceIn(MIN_TEMPO, MAX_TEMPO))
    }

    override fun createResizeCopy(beat: Float, width: Float): TempoChange {
        return TempoChange(container as TempoChanges, beat, bpm)
    }

    override fun getColour(theme: Theme): Color {
        return theme.trackers.tempoChange
    }

    fun secondsToBeats(seconds: Float): Float {
        return endBeat + BpmUtils.secondsToBeats(seconds - this.seconds, tempoAtSeconds(seconds))
    }

    fun beatsToSeconds(beat: Float): Float {
        return seconds + BpmUtils.beatsToSeconds(beat - this.endBeat, tempoAt(beat))
    }

    // Remnants of stretchable tempo changes

    fun tempoAt(beat: Float): Float {
        return bpm
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return bpm
    }

}