package io.github.chrislo27.rhre3.track.music

import io.github.chrislo27.rhre3.tracker.TrackerContainer


class MusicVolumes(val defaultVolume: Float = 1f) : TrackerContainer<MusicVolumeChange>() {

    fun getVolume(beat: Float): Float {
        val change = map.floorEntry(beat)?.value ?: return defaultVolume
        return change.volume
    }

}