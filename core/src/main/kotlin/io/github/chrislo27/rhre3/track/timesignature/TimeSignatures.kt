package io.github.chrislo27.rhre3.track.timesignature

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.tracker.TrackerContainer


class TimeSignatures : TrackerContainer<TimeSignature>() {

    override fun toTree(node: ObjectNode): ObjectNode {
        val arrayNode = node.putArray("trackers")

        map.values.forEach {
            arrayNode.addObject()
                    .put("beat", it.beat.toInt())
                    .put("measure", it.measure)
                    .put("upper", it.upper)
        }

        return node
    }

    override fun fromTree(node: ObjectNode) {
        (node["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
            add(TimeSignature(it["beat"].asDouble().toInt(), it["upper"].asInt(4)))
        }
    }

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