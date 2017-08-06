package io.github.chrislo27.rhre3.tempo

import io.github.chrislo27.rhre3.tracker.Tracker


class TempoChange(beat: Float, val bpm: Float) : Tracker(beat) {

    var seconds: Float = 0f // should be lateinit

    override fun toString(): String {
        return "[beat=$beat,bpm=$bpm,seconds=$seconds]"
    }
}