package io.github.chrislo27.rhre3.track.music

import io.github.chrislo27.rhre3.tracker.Tracker


class MusicVolumeChange(beat: Float, volume: Float) : Tracker(beat) {

    val volume: Float = volume.coerceIn(0f, 1f)

}