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

    init {
        text = "♬\ue13c➡$volume%"
    }

    override fun scroll(amount: Int, control: Boolean, shift: Boolean): MusicVolumeChange? {
        val change = amount * (if (control) 5 else 1)

        if ((change < 0 && volume <= 0) || (change > 0 && volume >= MAX_VOLUME))
            return null

        return MusicVolumeChange(container, beat, width, (volume + change).coerceIn(0, MAX_VOLUME))
    }

    override fun createResizeCopy(beat: Float, width: Float): MusicVolumeChange {
        return MusicVolumeChange(container, beat, width, volume)
    }

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