package io.github.chrislo27.rhre3.track.timesignature

import io.github.chrislo27.rhre3.tracker.TrackerContainer


class TimeSignatures : TrackerContainer<TimeSignature>() {

    fun getTimeSignature(beat: Float): TimeSignature? {
        return map.floorEntry(beat)?.value
    }

    fun getMeasure(beat: Float): Int {
        val intBeat = beat.toInt()
        if (intBeat < 0)
            return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = intBeat - timeSig.beat.toInt()

        // currently assumes X/4 time only
        return beatDiff / timeSig.upper + timeSig.measure
    }

    fun getMeasurePart(beat: Float): Int {
        val intBeat = beat.toInt()
        if (intBeat < 0)
            return -1
        val timeSig = getTimeSignature(beat) ?: return -1
        val beatDiff = intBeat - timeSig.beat.toInt()

        // currently assumes X/4 time only
        return beatDiff % timeSig.upper
    }

    override fun update() {
        super.update()

        val old = map.values.toList()

        map.clear()

        old.forEach {
            if (map.isEmpty()) {
                it.measure = 1
            } else {
                val below: TimeSignature = map.lowerEntry(it.beat).value!!
                val measure = getMeasure(it.beat)

                it.measure = measure + (if (getMeasurePart(it.beat) == 0) 0 else 1)
            }

            map[it.beat] = it
        }
    }
}