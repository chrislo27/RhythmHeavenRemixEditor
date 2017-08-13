package io.github.chrislo27.rhre3.editor.action

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntityPlaceAction(val editor: Editor, val entities: List<Entity>) : ReversibleAction<Remix> {

    override fun redo(context: Remix) {
        context.entities.addAll(entities)
    }

    override fun undo(context: Remix) {
        context.entities.removeAll(entities)
    }

}
