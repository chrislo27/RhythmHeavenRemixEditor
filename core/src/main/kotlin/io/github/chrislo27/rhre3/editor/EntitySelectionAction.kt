package io.github.chrislo27.rhre3.editor

import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntitySelectionAction(val editor: Editor, val old: List<Entity>, val new: List<Entity>)
    : ReversibleAction<Remix> {

    override fun redo(context: Remix) {
        old.forEach { it.isSelected = false }
        new.forEach { it.isSelected = true }
    }

    override fun undo(context: Remix) {
        old.forEach { it.isSelected = true }
        new.forEach { it.isSelected = false }
    }
}