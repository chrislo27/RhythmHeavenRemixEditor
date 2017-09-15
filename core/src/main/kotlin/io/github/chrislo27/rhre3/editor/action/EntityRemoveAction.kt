package io.github.chrislo27.rhre3.editor.action

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntityRemoveAction(val editor: Editor, val entities: List<Entity>, val oldPos: List<Rectangle>)
    : ReversibleAction<Remix> {

    override fun undo(context: Remix) {
        context.entities.addAll(entities)
        entities.forEachIndexed { index, entity ->
            entity.updateBounds {
                entity.bounds.set(oldPos[index])
            }
        }
        context.recomputeCachedData()
    }

    override fun redo(context: Remix) {
        context.entities.removeAll(entities)
        context.recomputeCachedData()
    }

}
