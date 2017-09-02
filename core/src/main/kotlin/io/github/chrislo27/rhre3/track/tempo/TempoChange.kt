package io.github.chrislo27.rhre3.track.tempo

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.oopsies.GroupedAction
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.tracker.Tracker
import io.github.chrislo27.rhre3.tracker.TrackerExistenceAction


class TempoChange(beat: Float, val bpm: Float) : Tracker(beat) {

    var seconds: Float = 0f // should be lateinit

    private val renderText = "♩= ${String.format("%.1f", bpm)}"

    override fun onScroll(remix: Remix, amount: Int, shift: Boolean,
                          control: Boolean) {
        val newBpm = (bpm + amount * (if (control) 5f else 1f) * (if (shift) 0.1f else 1f)).coerceIn(30f, 600f)
        remix.mutate(GroupedAction(listOf(
                TrackerExistenceAction(remix, remix.tempos,
                                       this, true),
                TrackerExistenceAction(remix, remix.tempos,
                                       TempoChange(beat, newBpm), false)
                                         )))
    }

    override fun toString(): String {
        return "[beat=$beat,bpm=$bpm,seconds=$seconds]"
    }

    override fun getColor(theme: Theme): Color {
        return theme.trackers.tempoChange
    }

    override fun getRenderText(): String {
        return renderText
    }
}