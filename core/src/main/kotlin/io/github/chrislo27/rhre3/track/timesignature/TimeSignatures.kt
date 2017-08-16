package io.github.chrislo27.rhre3.track.timesignature

import io.github.chrislo27.rhre3.tracker.TrackerContainer


class TimeSignatures : TrackerContainer<TimeSignature>() {

    fun getTimeSignature(beat: Float): TimeSignature? {
        return map.floorEntry(beat)?.value
    }

}