package io.github.chrislo27.rhre3.track.timesignature

import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class TimeSignatureAction(val remix: Remix, val timeSig: TimeSignature, val remove: Boolean) : ReversibleAction<Remix> {

    private val container: TimeSignatures
        get() = remix.timeSignatures

    private fun add() {
        container.add(timeSig)
    }

    private fun remove() {
        container.remove(timeSig)
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