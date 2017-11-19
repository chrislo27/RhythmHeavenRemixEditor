package io.github.chrislo27.rhre3.track.timesignature

import java.util.*


class TimeSignatures {

    val map: NavigableMap<Int, TimeSignature> = TreeMap()

    fun clear() {
        map.clear()
    }

    fun add(tracker: TimeSignature) {
        map.put(tracker.beat, tracker)
        update()
    }

    fun remove(tracker: TimeSignature) {
        map.remove(tracker.beat, tracker)
        update()
    }

    fun remove(beat: Int) {
        map.remove(beat)
        update()
    }

    fun getTimeSignature(beat: Float): TimeSignature? {
        return map.floorEntry(Math.floor(beat.toDouble()).toInt())?.value
    }

    fun getMeasure(beat: Float): Int {
        val intBeat = beat.toInt()
        if (intBeat < 0)
            return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = intBeat - timeSig.beat

        // currently assumes X/4 time only
        return beatDiff / timeSig.divisions + timeSig.measure
    }

    fun getMeasurePart(beat: Float): Int {
        val intBeat = beat.toInt()
        if (intBeat < 0)
            return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = intBeat - timeSig.beat

        // currently assumes X/4 time only
        return beatDiff % timeSig.divisions
    }

    fun update() {
        val old = map.values.toList()

        map.clear()

        old.forEach {
            if (map.isEmpty()) {
                it.measure = 1
            } else {
                val measure = getMeasure(it.beat.toFloat())

                it.measure = measure + (if (getMeasurePart(it.beat.toFloat()) == 0) 0 else 1)
            }

            map[it.beat] = it
        }
    }

    fun get(beat: Float): TimeSignature? =
            map[Math.floor(beat.toDouble()).toInt()]

    fun lowerGet(beat: Float): TimeSignature? =
            map.lowerEntry(Math.floor(beat.toDouble()).toInt())?.value

    fun higherGet(beat: Float): TimeSignature? =
            map.higherEntry(Math.floor(beat.toDouble()).toInt())?.value

}