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

    open fun add(tracker: T, shouldUpdate: Boolean = true): Boolean {
        if (map.containsKey(tracker.beat))
            return false

        backingMap[tracker.beat] = tracker

        if (shouldUpdate) {
            update()
        }

        return true
    }

    open fun remove(tracker: T, shouldUpdate: Boolean = true): Boolean {
        val didRemove = backingMap.remove(tracker.beat, tracker)

        if (didRemove && shouldUpdate) {
            update()
        }

        return didRemove
    }

}
