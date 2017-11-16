package io.github.chrislo27.rhre3.track.tracker.music

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.track.tracker.Tracker


class MusicVolumeChange(beat: Float, volume: Int) : Tracker(beat) {

    var volume: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            renderText = "♬\ue13c➡$field%"
        }
    private var renderText: String = ""

    init {
        this.volume = volume
    }

    override fun onScroll(remix: Remix, amount: Int, shift: Boolean,
                          control: Boolean) {
        remix.mutate(object : ReversibleAction<Remix> {
            val oldVol = volume

            override fun redo(context: Remix) {
                volume += amount * if (control) 5 else 1
            }

            override fun undo(context: Remix) {
                volume = oldVol
            }
        })
    }

    override fun getColor(theme: Theme): Color {
        return theme.trackers.musicVolume
    }

    override fun getRenderText(): String {
        return renderText
    }
}