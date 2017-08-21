package io.github.chrislo27.rhre3.editor.action

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntityRepitchAction(val editor: Editor, val entities: List<Entity>, val oldPitches: List<Int>)
    : ReversibleAction<Remix> {

    val newPitches: List<Int> = entities.map { (it as? IRepitchable)?.semitone ?: 0 }

    override fun redo(context: Remix) {
        entities.forEachIndexed { index, entity ->
            (entity as? IRepitchable)?.semitone = newPitches[index]
        }
    }

    override fun undo(context: Remix) {
        entities.forEachIndexed { index, entity ->
            (entity as? IRepitchable)?.semitone = oldPitches[index]
        }
    }
}