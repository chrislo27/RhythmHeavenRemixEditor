package io.github.chrislo27.rhre3.tracker

import java.util.*


open class TrackerContainer<T : Tracker> {

    protected val map: NavigableMap<Float, T> = TreeMap()

    open fun clear() {
        map.clear()
    }

    open fun add(tracker: T) {
        map.put(tracker.beat, tracker)
        update()
    }

    open fun remove(tracker: T) {
        map.remove(tracker.beat, tracker)
        update()
    }

    open fun remove(beat: Float) {
        map.remove(beat)
        update()
    }

    protected open fun update() {
        // required for tempo changes
    }

    fun getBackingMap(): NavigableMap<Float, T> =
            map

    fun get(beat: Float): T? =
            map[beat]

    fun lowerGet(beat: Float): T? =
            map.lowerEntry(beat)?.value

    fun higherGet(beat: Float): T? =
            map.higherEntry(beat)?.value

}