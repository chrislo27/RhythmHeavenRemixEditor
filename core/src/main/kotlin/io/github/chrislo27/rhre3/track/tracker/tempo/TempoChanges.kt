package io.github.chrislo27.rhre3.track.tracker.tempo

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer
import io.github.chrislo27.rhre3.util.Swing
import io.github.chrislo27.rhre3.util.TempoUtils
import java.util.*


class TempoChanges(val defaultTempo: Float = 120f, val defaultSwing: Swing = Swing.STRAIGHT) : TrackerContainer<TempoChange>(0) {

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
                    .put("swingRatio", it.swing.ratio)
                    .put("swingDivision", it.swing.division)
        }

        return node
    }

    override fun fromTree(node: ObjectNode) {
        (node["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
            val swingRatio: Int = it["swingRatio"]?.asInt(0) ?: 0
            val swingDivision: Float = it["swingDivision"]?.asDouble(0.0)?.toFloat() ?: 0f
            val swing: Swing = if (swingRatio !in Swing.ABS_MIN_SWING..Swing.MAX_SWING || swingDivision <= 0f) defaultSwing else Swing(swingRatio, swingDivision)
            add(TempoChange(this,
                            it["beat"].asDouble().toFloat(),
                            it["bpm"].asDouble(defaultTempo.toDouble()).toFloat(),
                            swing),
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
            it.seconds = previous?.beatsToSeconds(it.beat) ?: TempoUtils.beatsToSeconds(it.beat, defaultTempo) // If not present, use straight beats

            backingMap[it.beat] = it
            backingSecondsMap[it.seconds] = it
        }
    }

    fun secondsToBeats(seconds: Float): Float {
        val tc: TempoChange = backingSecondsMap.lowerEntry(seconds)?.value ?: return TempoUtils.secondsToBeats(seconds, defaultTempo)

        return tc.secondsToBeats(seconds)
    }

    fun beatsToSeconds(beat: Float): Float {
        val tc: TempoChange = backingMap.floorEntry(beat)?.value ?: return TempoUtils.beatsToSeconds(beat, defaultTempo)

        return tc.beatsToSeconds(beat)
    }

    fun tempoAt(beat: Float): Float {
        return backingMap.lowerEntry(beat)?.value?.tempoAt(beat) ?: defaultTempo
    }

    fun tempoAtSeconds(seconds: Float): Float {
        return backingSecondsMap.lowerEntry(seconds)?.value?.tempoAtSeconds(seconds) ?: defaultTempo
    }

    fun swingAt(beat: Float): Swing {
        return backingMap.lowerEntry(beat)?.value?.swing ?: defaultSwing
    }

    fun swingAtSeconds(seconds: Float): Swing {
        return backingSecondsMap.lowerEntry(seconds)?.value?.swing ?: defaultSwing
    }
}