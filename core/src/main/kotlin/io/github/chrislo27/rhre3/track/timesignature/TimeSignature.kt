package io.github.chrislo27.rhre3.track.timesignature

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.tracker.Tracker
import io.github.chrislo27.toolboks.Toolboks


class TimeSignature(beat: Int, upper: Int) : Tracker(beat.toFloat()) {

    var upper: Int = upper
        set(value) {
            field = value.coerceIn(1, 64)
            updateRenderText()
        }
    var lower: Int = 4
        private set(value) {
            field = value.coerceIn(1, 32)
            if (field != 4) {
                Toolboks.LOGGER.warn("Time signature bottom number isn't 4 ($field)")
            }
            updateRenderText()
        }

    var measure: Int = -1 // should be lateinit

    private var renderText: String = ""

    init {
        updateRenderText()
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