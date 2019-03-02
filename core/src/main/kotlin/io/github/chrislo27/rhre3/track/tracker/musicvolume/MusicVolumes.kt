package io.github.chrislo27.rhre3.track.tracker.musicvolume

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer


class MusicVolumes : TrackerContainer<MusicVolumeChange>(1) {
    override fun toTree(node: ObjectNode): ObjectNode {
        val arrayNode = node.putArray("trackers")

        map.values.forEach {
            arrayNode.addObject()
                    .put("beat", it.beat)
                    .put("width", it.width)
                    .put("volume", it.volume)
        }

        return node
    }

    override fun fromTree(node: ObjectNode) {
        clear()
        (node["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
            add(MusicVolumeChange(this,
                                  it["beat"].asDouble().toFloat(),
                                  it["width"]?.asDouble(0.0)?.toFloat() ?: 0f,
                                  it["volume"].asInt(100).coerceIn(0, MusicVolumeChange.MAX_VOLUME)))
        }
    }

    override fun update() {
    }

    fun volumeAt(beat: Float): Float {
        return backingMap.lowerEntry(beat)?.value?.volumeAt(beat) ?: 1f
    }
}