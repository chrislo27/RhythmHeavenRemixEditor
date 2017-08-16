package io.github.chrislo27.rhre3.tempo

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.tracker.Tracker


class TempoChange(beat: Float, val bpm: Float) : Tracker(beat) {

    var seconds: Float = 0f // should be lateinit

    private val renderText = "♩=${String.format("%.1f", bpm)}"

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