package io.github.chrislo27.rhre3.track.tracker.tempo

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.util.BpmUtils
import java.util.*


class TempoChange(container: TempoChanges, beat: Float, width: Float, val bpm: Float)
    : Tracker<TempoChange>(container, beat, width) {

    companion object {

        fun getSecondsDuration(beatWidth: Float, startBpm: Float, endBpm: Float): Float {
            return ((2 * beatWidth) / (startBpm + endBpm)) * 60f
        }

        fun getBeatDuration(secondsWidth: Float, startBpm: Float, endBpm: Float): Float {
            return (secondsWidth / 60f) * (startBpm + endBpm) / 2f
        }

    }

    var seconds: Float = 0f
    var widthSeconds: Float = 0f

    val endSeconds: Float
        get() = seconds + widthSeconds

    val previousBpm: Float
        get() = (container.map as NavigableMap).lowerEntry(beat)?.value?.bpm ?: (container as TempoChanges).defaultTempo

    override fun getColour(theme: Theme): Color {
        return theme.trackers.tempoChange
    }

    fun secondsToBeats(seconds: Float): Float {
        val secondsWidth = seconds - this.seconds
        return if (seconds >= endSeconds) {
            endBeat + BpmUtils.secondsToBeats(seconds - this.endSeconds, tempoAtSeconds(seconds))
        } else {
            beat + BpmUtils.secondsToBeats(secondsWidth, previousBpm) + getBeatDuration(secondsWidth, previousBpm, tempoAtSeconds(seconds))
        }
    }

    fun beatsToSeconds(beat: Float): Float {
        return if (beat >= endBeat) {
            endSeconds + BpmUtils.beatsToSeconds(beat - this.endBeat, tempoAt(beat))
        } else {
            seconds + getSecondsDuration(beat - this.beat, previousBpm, tempoAt(beat))
        }
    }

    fun tempoAt(beat: Float): Float {
        val endBeat = this.endBeat
        return if (beat in this.beat..endBeat) {
            MathUtils.lerp(this.previousBpm, this.bpm, (beat - this.beat) / width)
        } else {
            bpm
        }
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return if (seconds in this.seconds..this.endSeconds) {
            MathUtils.lerp(this.previousBpm, this.bpm, (seconds - this.seconds) / widthSeconds)
        } else {
            bpm
        }
    }

}