package io.github.chrislo27.rhre3.track.tracker.tempo

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer
import io.github.chrislo27.rhre3.util.BpmUtils
import java.util.*


class TempoChanges(val defaultTempo: Float = 120f) : TrackerContainer<TempoChange>(0) {

    private val backingSecondsMap: NavigableMap<Float, TempoChange> = TreeMap()
    val secondsMap: Map<Float, TempoChange>
        get() = backingSecondsMap

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
            add(TempoChange(this,
                            it["beat"].asDouble().toFloat(),
                            it["bpm"].asDouble(defaultTempo.toDouble()).toFloat()),
                shouldUpdate = false)
        }
        update()
    }

    override fun update() {
        val old = map.values.toList()

        backingMap.clear()
        backingSecondsMap.clear()

        old.forEach {
            val previous: TempoChange? = backingMap.lowerEntry(it.beat)?.value
            it.seconds = previous?.beatsToSeconds(it.beat) ?: BpmUtils.beatsToSeconds(it.beat, defaultTempo)

            backingMap[it.beat] = it
            backingSecondsMap[it.seconds] = it
        }
    }

    fun secondsToBeats(seconds: Float): Float {
        val tc: TempoChange = backingSecondsMap.lowerEntry(seconds)?.value ?: return BpmUtils.secondsToBeats(seconds, defaultTempo)

        return tc.secondsToBeats(seconds)
    }

    fun beatsToSeconds(beat: Float): Float {
        val tc: TempoChange = backingMap.floorEntry(beat)?.value ?: return BpmUtils.beatsToSeconds(beat, defaultTempo)

        return tc.beatsToSeconds(beat)
    }

    fun tempoAt(beat: Float): Float {
        return backingMap.lowerEntry(beat)?.value?.tempoAt(beat) ?: defaultTempo
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return backingSecondsMap.lowerEntry(seconds)?.value?.tempoAtSeconds(seconds) ?: defaultTempo
    }
}