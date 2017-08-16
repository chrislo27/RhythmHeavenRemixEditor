package io.github.chrislo27.rhre3.track.music

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.tracker.Tracker


class MusicVolumeChange(beat: Float, volume: Int) : Tracker(beat) {

    var volume: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            renderText = "$field% ♬"
        }
    private var renderText: String = ""

    init {
        this.volume = volume
    }

    override fun getColor(theme: Theme): Color {
        return theme.trackers.musicVolume
    }

    override fun getRenderText(): String {
        return renderText
    }
}