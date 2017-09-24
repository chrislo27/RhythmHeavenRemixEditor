package io.github.chrislo27.rhre3.track.tracker.music

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer


class MusicVolumes(val defaultVolume: Int = 100) : TrackerContainer<MusicVolumeChange>() {

    override fun toTree(node: ObjectNode): ObjectNode {
        val arrayNode = node.putArray("trackers")

        map.values.forEach {
            arrayNode.addObject()
                    .put("beat", it.beat)
                    .put("volume", it.volume)
        }

        return node
    }

    override fun fromTree(node: ObjectNode) {
        (node["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
            add(MusicVolumeChange(it["beat"].asDouble().toFloat(), it["volume"].asInt(100).coerceIn(0, 100)))
        }
    }

    fun getVolume(beat: Float): Int {
        val change = map.floorEntry(beat)?.value ?: return defaultVolume
        return change.volume
    }

    fun getPercentageVolume(beat: Float): Float {
        return getVolume(beat) / 100f
    }

}