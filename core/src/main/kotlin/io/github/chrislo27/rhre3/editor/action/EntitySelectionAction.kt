package io.github.chrislo27.rhre3.editor.action

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntitySelectionAction(val editor: Editor, val old: List<Entity>, val new: List<Entity>)
    : ReversibleAction<Remix> {

    override fun redo(context: Remix) {
        editor.selection = new.toList()
    }

    override fun undo(context: Remix) {
        editor.selection = old.toList()
    }
}