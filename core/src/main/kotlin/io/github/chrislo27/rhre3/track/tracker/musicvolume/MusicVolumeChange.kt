package io.github.chrislo27.rhre3.track.tracker.musicvolume

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer
import java.util.*


class MusicVolumeChange(container: TrackerContainer<MusicVolumeChange>, beat: Float, width: Float, volume: Int)
    : Tracker<MusicVolumeChange>(container, beat, width) {

    companion object {
        val MAX_VOLUME: Int = 200
    }

    val volume: Int = volume.coerceIn(0, MAX_VOLUME)
    val previousVolume: Int
        get() = (container.map as NavigableMap).lowerEntry(beat)?.value?.volume ?: 100

    override fun getColour(theme: Theme): Color {
        return theme.trackers.musicVolume
    }

    fun volumeAt(beat: Float): Float {
        val endBeat = this.endBeat
        return if (beat <= endBeat) {
            MathUtils.lerp(previousVolume / 100f, volume / 100f, (beat - this.beat) / width)
        } else {
            volume / 100f
        }
    }

}