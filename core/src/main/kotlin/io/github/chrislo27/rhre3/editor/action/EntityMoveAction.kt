package io.github.chrislo27.rhre3.editor.action

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntityMoveAction(val editor: Editor, val entities: List<Entity>, val oldPos: List<Rectangle>)
    : ReversibleAction<Remix> {

    private val newPos = entities.map { Rectangle(it.bounds) }

    override fun redo(context: Remix) {
        entities.forEachIndexed { i, it ->
            it.updateBounds {
                it.bounds.set(newPos[i])
            }
        }
    }

    override fun undo(context: Remix) {
        entities.forEachIndexed { i, it ->
            it.updateBounds {
                it.bounds.set(oldPos[i])
            }
        }
    }

}
