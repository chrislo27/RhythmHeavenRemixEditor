package io.github.chrislo27.rhre3.track.tracker

import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class TrackerValueChange(val original: Tracker<*>, var current: Tracker<*>)
    : ReversibleAction<Remix> {

    private val container = original.container

    override fun redo(context: Remix) {
        container.remove(original, false)
        container.add(current, true)
    }

    override fun undo(context: Remix) {
        container.remove(current, false)
        container.add(original, true)
    }
}
