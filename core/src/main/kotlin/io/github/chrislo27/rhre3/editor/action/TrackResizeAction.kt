package io.github.chrislo27.rhre3.editor.action

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class TrackResizeAction(val editor: Editor, val oldSize: Int, val newSize: Int)
    : ReversibleAction<Remix> {

    override fun redo(context: Remix) {
        context.trackCount = newSize
    }

    override fun undo(context: Remix) {
        context.trackCount = oldSize
    }

}
