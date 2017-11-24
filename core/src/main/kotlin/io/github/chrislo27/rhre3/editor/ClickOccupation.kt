package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.toolboks.util.gdxutils.intersects


/**
 * This is where most of the editor logic resides. A click can have an "occupation" (hence the name), and
 * prevents other occupations from occurring at the same time (ex: playback start move and music start move).
 */
sealed class ClickOccupation {

    interface TrackerBased {
        var finished: Boolean
        var final: Float
    }

    object None : ClickOccupation()

    class Playback(val editor: Editor)
        : ClickOccupation(), ReversibleAction<Remix>, TrackerBased {
        val old = editor.remix.playbackStart
        override var finished: Boolean = false
        override var final: Float = Float.NaN
            set(value) {
                if (!java.lang.Float.isNaN(field)) {
                    error("Attempt to set value to $value when already set to $field")
                }
                field = value
            }

        override fun redo(context: Remix) {
            if (final == Float.NaN)
                error("Final value was NaN which is impossible")
            editor.remix.playbackStart = final
        }

        override fun undo(context: Remix) {
            editor.remix.playbackStart = old
        }
    }

    class Music(val editor: Editor, val middleClick: Boolean)
        : ClickOccupation(), ReversibleAction<Remix>, TrackerBased {
        val old = editor.remix.musicStartSec
        override var finished: Boolean = false
        override var final: Float = Float.NaN
            set(value) {
                if (!java.lang.Float.isNaN(field)) {
                    error("Attempt to set value to $value when already set to $field")
                }
                field = value
            }

        override fun redo(context: Remix) {
            if (final != final)
                error("Final value was NaN which is impossible")
            editor.remix.musicStartSec = final
        }

        override fun undo(context: Remix) {
            editor.remix.musicStartSec = old
        }
    }

    class CreatingSelection(val editor: Editor,
                            val startPoint: Vector2) : ClickOccupation() {
        val oldSelection = editor.selection.toList()
        val rectangle = Rectangle()

        fun updateRectangle() {
            val startX = startPoint.x
            val startY = startPoint.y
            val width = editor.remix.camera.getInputX() - startX
            val height = editor.remix.camera.getInputY() - startY

            if (width < 0) {
                val abs = Math.abs(width)
                rectangle.x = startX - abs
                rectangle.width = abs
            } else {
                rectangle.x = startX
                rectangle.width = width
            }

            if (height < 0) {
                val abs = Math.abs(height)
                rectangle.y = startY - abs
                rectangle.height = abs
            } else {
                rectangle.y = startY
                rectangle.height = height
            }
        }

    }

    class SelectionDrag(val editor: Editor,
                        private val first: Entity,
                        val mouseOffset: Vector2,
                        val isNew: Boolean,
                        val isCopy: Boolean,
                        val previousSelection: List<Entity>,
                        val stretchType: StretchRegion)
        : ClickOccupation() {

        companion object {
            fun copyBounds(selection: List<Entity>): List<Rectangle> =
                    selection.map { Rectangle(it.bounds) }
        }

        val isNewOrCopy: Boolean = isNew || isCopy
        val oldBounds: List<Rectangle> = copyBounds(editor.selection)
        val isStretching: Boolean by lazy { !isNewOrCopy && stretchType != StretchRegion.NONE }
        private val selection: List<Entity>
            get() = editor.selection
        val isAllSpecial: Boolean by lazy {
            selection.all { it is ModelEntity<*> && it.isSpecialEntity && it !is EndEntity }
        }
        val isBottomSpecial: Boolean by lazy {
            if (isAllSpecial) return@lazy true
            val lowPoint = selection.minBy { it.bounds.y }?.bounds?.y ?: error("Nothing in selection")
            val allBottom = selection.filter { MathUtils.isEqual(it.bounds.y, lowPoint) }

            allBottom.all { it is ModelEntity<*> && it.isSpecialEntity && it !is EndEntity }
        }

        val left: Float
            get() = selection.minBy { it.bounds.x }?.bounds?.x ?: error("Nothing in selection")

        val right: Float
            get() {
                val right = selection.maxBy { it.bounds.x + it.bounds.width } ?: error("Nothing in selection")
                return right.bounds.x + right.bounds.width
            }

        val top: Float
            get() {
                val highest = selection.maxBy { it.bounds.y } ?: error("Nothing in selection")
                return highest.bounds.y + highest.bounds.height
            }

        val bottom: Float
            get() = selection.minBy { it.bounds.y }?.bounds?.y ?: error("Nothing in selection")

        val width: Float by lazy {
            right - left
        }
        val height: Int by lazy {
            Math.round(top - bottom)
        }

        fun setFirstPosition(x: Float, y: Float) {
            // reducing object creation due to rapid calling
            val oldFirstPosX = first.bounds.x
            val oldFirstPosY = first.bounds.y
            first.updateBounds {
                first.bounds.setPosition(x, y)
            }

            selection.forEach { entity ->
                if (entity === first)
                    return@forEach
                entity.updateBounds {
                    entity.bounds.x = (entity.bounds.x - oldFirstPosX) + x
                    entity.bounds.y = (entity.bounds.y - oldFirstPosY) + y
                }
            }
        }

        fun setPositionRelativeToMouse(snap: Float = editor.snap, intY: Boolean = true) {
            val y = editor.remix.camera.getInputY() - mouseOffset.y
            setFirstPosition(MathHelper.snapToNearest(editor.remix.camera.getInputX() - mouseOffset.x, snap),
                             if (intY) Math.round(y).toFloat() else y)
        }

        fun isPlacementValid(): Boolean {
            if (isInDeleteZone())
                return false
            if (top > Editor.TRACK_COUNT)
                return false

            // EXCEPTIONS for special entities
            if (selection.any { it is EndEntity } && editor.remix.entities.filter { it is EndEntity }.size > 1)
                return false

            return editor.remix.entities.all {
                it in selection || selection.all { sel ->
                    !sel.bounds.intersects(it.bounds)
                }
            }
        }

        fun isInDeleteZone(): Boolean {
            return if (isBottomSpecial) {
                bottom < -1.5f
            } else {
                bottom < -0.5f
            }
        }

    }


}
