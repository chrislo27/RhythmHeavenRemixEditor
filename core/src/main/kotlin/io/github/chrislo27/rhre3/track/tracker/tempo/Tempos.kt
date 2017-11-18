package io.github.chrislo27.rhre3.track.tracker.tempo

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer
import io.github.chrislo27.rhre3.util.BpmUtils
import java.util.*


class Tempos(val defaultTempo: Float = 120f) : TrackerContainer<TempoChange>(0) {

    private val secondsMap: NavigableMap<Float, TempoChange> = TreeMap()

    override fun toTree(node: ObjectNode): ObjectNode {
        val arrayNode = node.putArray("trackers")

        map.values.forEach {
            arrayNode.addObject()
                    .put("beat", it.beat)
                    .put("seconds", it.seconds)
                    .put("bpm", it.bpm)
        }

        return node
    }

    override fun fromTree(node: ObjectNode) {
        (node["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
            add(TempoChange(it["beat"].asDouble().toFloat(), it["bpm"].asDouble(120.0).toFloat()))
        }
    }

    override fun clear() {
        super.clear()
        secondsMap.clear()
    }

    override fun update() {
        super.update()

        val old = map.values.toList()

        map.clear()
        secondsMap.clear()

        old.forEach {
            if (map.isEmpty()) {
                it.seconds = BpmUtils.beatsToSeconds(it.beat, defaultTempo)
            } else {
                val below: TempoChange = map.lowerEntry(it.beat).value!!

                it.seconds = BpmUtils.beatsToSeconds(it.beat - below.beat, below.bpm) + below.seconds
            }

            map[it.beat] = it
            secondsMap[it.seconds] = it
        }
    }

    fun beatsToSeconds(beat: Float): Float {
        val tc: TempoChange = map.floorEntry(beat)?.value ?:
                return BpmUtils.beatsToSeconds(beat, defaultTempo)

        return tc.seconds + BpmUtils.beatsToSeconds(beat - tc.beat, tc.bpm)
    }

    fun secondsToBeats(seconds: Float): Float {
        val tc: TempoChange = secondsMap.lowerEntry(seconds)?.value ?:
                return BpmUtils.secondsToBeats(seconds, defaultTempo)

        return tc.beat + BpmUtils.secondsToBeats(seconds - tc.seconds, tc.bpm)
    }

    fun tempoAt(beat: Float): Float {
        return map.lowerEntry(beat)?.value?.bpm ?: defaultTempo
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return secondsMap.lowerEntry(seconds)?.value?.bpm ?: defaultTempo
    }
}