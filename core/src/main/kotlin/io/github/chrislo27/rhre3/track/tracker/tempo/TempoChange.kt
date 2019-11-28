package io.github.chrislo27.rhre3.track.tracker.tempo

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.util.Swing
import io.github.chrislo27.rhre3.util.SwingUtils
import io.github.chrislo27.rhre3.util.TempoUtils
import java.util.*


class TempoChange(container: TempoChanges, beat: Float, val bpm: Float, val swing: Swing, width: Float)
    : Tracker<TempoChange>(container, beat, width) {

    companion object {
        val MIN_TEMPO: Float = 1.0f
        val MAX_TEMPO: Float = 600f

        fun getSecondsDuration(beatWidth: Float, startBpm: Float, endBpm: Float): Float {
            return ((2 * beatWidth) / (startBpm + endBpm)) * 60f
        }

        fun getBeatDuration(secondsWidth: Float, startBpm: Float, endBpm: Float): Float {
            return (secondsWidth / 60f) * (startBpm + endBpm) / 2f
        }

        fun getFormattedText(bpm: Float): String = "♩=${Editor.ONE_TO_TWO_DECIMAL_PLACES_FORMATTER.format(bpm)}"
    }

    override val allowsResize: Boolean = true
    var seconds: Float = 0f
    var widthSeconds: Float = 0f

    val endSeconds: Float
        get() = seconds + widthSeconds

    val previousBpm: Float
        get() = (container.map as NavigableMap).lowerEntry(beat)?.value?.bpm ?: (container as TempoChanges).defaultTempo

    init {
        text = getFormattedText(bpm)
    }

    override fun scroll(amount: Int, control: Boolean, shift: Boolean): TempoChange? {
        val change = amount * (if (shift) (if (control) 0.05f else 0.01f) else (if (control) 5f else 1f))

        if ((change < 0 && bpm <= MIN_TEMPO) || (change > 0 && bpm >= MAX_TEMPO))
            return null

        return TempoChange(container as TempoChanges, beat, (bpm + change).coerceIn(MIN_TEMPO, MAX_TEMPO), swing, width)
    }

    fun scrollSwing(amount: Int, control: Boolean, shift: Boolean): TempoChange? {
        if (shift && !control) {
            return TempoChange(container as TempoChanges, beat, bpm, swing.copy(division = if (swing.division == Swing.EIGHTH_DIVISION) Swing.SIXTEENTH_DIVISION else Swing.EIGHTH_DIVISION), width)
        } else if (control) {
            val change = amount * (if (shift) 5 else 1)
            if ((change < 0 && swing.ratio > Swing.MIN_SWING) || (change > 0 && swing.ratio < Swing.MAX_SWING)) {
                return TempoChange(container as TempoChanges, beat, bpm, swing.copy(ratio = (swing.ratio + change).coerceIn(Swing.MIN_SWING, Swing.MAX_SWING)), width)
            }
        } else {
            val list = Swing.SWING_LIST
            val currentIndex: Int = if (swing.ratio < list.first().ratio) -1 else list.let { _ ->
                var last: Int = 0

                for (it in list.indices) {
                    if (this.swing.ratio > list[last].ratio)
                        last = it
                }

                last
            }

            val nextIndex: Int = if (currentIndex == -1) {
                0
            } else {
                val futureNext = currentIndex + amount
                when {
                    futureNext < 0 -> list.size - 1
                    futureNext >= list.size -> 0
                    else -> futureNext
                }
            }

            if (nextIndex != currentIndex) {
                return TempoChange(container as TempoChanges, beat, bpm, swing.copy(ratio = list[nextIndex].ratio), width)
            }
        }

        return null
    }

    override fun createResizeCopy(beat: Float, width: Float): TempoChange {
        return TempoChange(container as TempoChanges, beat, bpm, swing, width)
    }

    override fun getColour(theme: Theme): Color {
        return theme.trackers.tempoChange
    }

    fun secondsToBeats(seconds: Float): Float {
        val secondsWidth = seconds - this.seconds
        return if (seconds >= endSeconds) {
            endBeat + SwingUtils.linearToSwing(TempoUtils.secondsToBeats(seconds - this.endSeconds, tempoAtSeconds(seconds)), swing)
        } else {
            beat + SwingUtils.linearToSwing(getBeatDuration(secondsWidth, previousBpm, tempoAtSeconds(seconds)), swing)
        }
    }

    fun beatsToSeconds(beat: Float): Float {
        val beatWidth = beat - this.beat
        return if (beat >= endBeat) {
            endSeconds + TempoUtils.beatsToSeconds(SwingUtils.swingToLinear(beat - this.endBeat, swing), tempoAt(beat))
        } else {
            seconds + SwingUtils.swingToLinear(getSecondsDuration(beatWidth, previousBpm, tempoAt(beat)), swing)
        }
    }

    // Stretchable tempo changes

    fun tempoAt(beat: Float): Float {
        val endBeat = this.endBeat
        return if (!isZeroWidth && beat in this.beat..endBeat) {
            MathUtils.lerp(this.previousBpm, this.bpm, (beat - this.beat) / width)
        } else {
            bpm
        }
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return if (!isZeroWidth && seconds in this.seconds..this.endSeconds) {
            MathUtils.lerp(this.previousBpm, this.bpm, (seconds - this.seconds) / widthSeconds)
        } else {
            bpm
        }
    }

}