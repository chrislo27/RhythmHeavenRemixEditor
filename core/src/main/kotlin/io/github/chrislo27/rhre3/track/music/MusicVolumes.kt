package io.github.chrislo27.rhre3.track.music

import io.github.chrislo27.rhre3.tracker.TrackerContainer


class MusicVolumes(val defaultVolume: Int = 100) : TrackerContainer<MusicVolumeChange>() {

    fun getVolume(beat: Float): Int {
        val change = map.floorEntry(beat)?.value ?: return defaultVolume
        return change.volume
    }

    fun getPercentageVolume(beat: Float): Float {
        return getVolume(beat) / 100f
    }

}