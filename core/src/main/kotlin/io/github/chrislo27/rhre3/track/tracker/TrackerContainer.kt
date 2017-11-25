package io.github.chrislo27.rhre3.track.tracker

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*


abstract class TrackerContainer<T : Tracker<T>>(val renderLayer: Int) {

    protected val backingMap: NavigableMap<Float, T> = TreeMap()
    val map: Map<Float, T>
        get() = backingMap

    abstract fun toTree(node: ObjectNode): ObjectNode

    abstract fun fromTree(node: ObjectNode)

    abstract fun update()

    @Suppress("UNCHECKED_CAST")
    open fun add(tracker: Tracker<*>, shouldUpdate: Boolean = true): Boolean {
        if (map.containsKey(tracker.beat))
            return false

        backingMap.put(tracker.beat, tracker as T)

        if (shouldUpdate) {
            update()
        }

        return true
    }

    @Suppress("UNCHECKED_CAST")
    open fun remove(tracker: Tracker<*>, shouldUpdate: Boolean = true): Boolean {
        val didRemove = backingMap.remove(tracker.beat, tracker as T)

        if (didRemove && shouldUpdate) {
            update()
        }

        return didRemove
    }

}
