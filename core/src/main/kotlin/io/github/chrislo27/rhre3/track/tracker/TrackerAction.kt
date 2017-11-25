package io.github.chrislo27.rhre3.track.tracker

import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class TrackerAction(val tracker: Tracker<*>, val remove: Boolean) : ReversibleAction<Remix> {

    private val container: TrackerContainer<*> = tracker.container

    override fun redo(context: Remix) {
        if (remove) {
            container.remove(tracker)
        } else {
            container.add(tracker)
        }
    }

    override fun undo(context: Remix) {
        if (!remove) {
            container.remove(tracker)
        } else {
            container.add(tracker)
        }
    }
}
