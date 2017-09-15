package io.github.chrislo27.rhre3.tracker

import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class TrackerExistenceAction<T : Tracker>(val remix: Remix, val container: TrackerContainer<T>,
                                          val tracker: T, val remove: Boolean)
    : ReversibleAction<Remix> {

    private fun add() {
        container.add(tracker)
    }

    private fun remove() {
        container.remove(tracker)
    }

    override fun redo(context: Remix) {
        if (remove) {
            remove()
        } else {
            add()
        }
        context.recomputeCachedData()
    }

    override fun undo(context: Remix) {
        if (remove) {
            add()
        } else {
            remove()
        }
        context.recomputeCachedData()
    }
}