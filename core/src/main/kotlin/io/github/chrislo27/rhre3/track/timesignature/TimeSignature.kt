package io.github.chrislo27.rhre3.track.timesignature

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.tracker.Tracker


class TimeSignature(beat: Float) : Tracker(beat) {

    var upper: Int = 4
        set(value) {
            field = value.coerceIn(1, 64)
            updateRenderText()
        }
    var lower: Int = 4
        set(value) {
            field = value.coerceIn(1, 32)
            updateRenderText()
        }

    private var renderText: String = ""

    constructor(beat: Float, upper: Int, lower: Int) : this(beat) {
        this.upper = upper
        this.lower = lower
    }

    private fun updateRenderText() {
        renderText = "$upper/$lower"
    }

    override fun getColor(theme: Theme): Color {
        return theme.trackers.timeSignature
    }

    override fun getRenderText(): String {
        return renderText
    }
}