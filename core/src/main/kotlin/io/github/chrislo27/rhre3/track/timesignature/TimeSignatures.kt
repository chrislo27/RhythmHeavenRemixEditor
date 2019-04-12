package io.github.chrislo27.rhre3.track.timesignature

import java.util.*


class TimeSignatures {

    val map: NavigableMap<Float, TimeSignature> = TreeMap()

    fun clear() {
        map.clear()
    }

    fun add(tracker: TimeSignature) {
        map[tracker.beat] = tracker
        update()
    }

    fun remove(tracker: TimeSignature) {
        map.remove(tracker.beat, tracker)
        update()
    }

    fun remove(beat: Float) {
        map.remove(beat)
        update()
    }

    fun getTimeSignature(beat: Float): TimeSignature? {
        return map.floorEntry(beat)?.value
    }

    fun getMeasure(beat: Float): Int {
        if (beat < 0) return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = beat - timeSig.beat

        return (beatDiff / (timeSig.noteFraction * timeSig.beatsPerMeasure)).toInt() + timeSig.measure
    }

    fun getMeasurePart(beat: Float): Int {
        if (beat < 0) return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = beat - timeSig.beat

        return ((beatDiff / timeSig.noteFraction) % timeSig.beatsPerMeasure).toInt()
    }

    fun update() {
        val old = map.values.toList()

        map.clear()

        old.forEach {
            if (map.isEmpty()) {
                it.measure = 1
            } else {
                val measure = getMeasure(it.beat)

                it.measure = measure + (if (getMeasurePart(it.beat) == 0) 0 else 1)
            }

            map[it.beat] = it
        }
    }

    fun get(beat: Float): TimeSignature? =
            map[beat]

    fun lowerGet(beat: Float): TimeSignature? =
            map.lowerEntry(beat)?.value

    fun higherGet(beat: Float): TimeSignature? =
            map.higherEntry(beat)?.value

}