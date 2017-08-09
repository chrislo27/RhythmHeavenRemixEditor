package io.github.chrislo27.rhre3.entity.model

import chrislo27.rhre.oopsies.ReversibleAction
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix


abstract class MultipartEntity<out M : Datamodel>(remix: Remix, datamodel: M) : ModelEntity<M>(remix, datamodel) {

    override var playbackCompletion: PlaybackCompletion = super.playbackCompletion
        get() = super.playbackCompletion
        set(value) {
            field = value
            when (field) {
                PlaybackCompletion.FINISHED -> {
                    internal.forEach { it.playbackCompletion = PlaybackCompletion.FINISHED }
                }
                PlaybackCompletion.WAITING -> {
                    internal.forEach { it.playbackCompletion = PlaybackCompletion.WAITING }
                }
                else -> {
                }
            }
        }
    protected val internal: MutableList<Entity> = mutableListOf()

    protected open fun translateInternal(old: Rectangle, changeWidths: Boolean = false) {
        internal.forEach {
            it.bounds.x = (it.bounds.x - old.x) + bounds.x
            it.bounds.y = (it.bounds.y - old.y) + bounds.y

            if (changeWidths) {
                it.bounds.width = (it.bounds.width / old.width) * bounds.width
            }
        }
    }

    open fun createSplittingAction(): ReversibleAction<Remix> {
        return object : ReversibleAction<Remix> {

            val original = this@MultipartEntity
            val internalCache = internal.toList()

            override fun redo(context: Remix) {
                context.entities.remove(original)
                context.entities.addAll(internalCache)
            }

            override fun undo(context: Remix) {
                context.entities.removeAll(internalCache)
                context.entities.add(original)
            }

        }
    }

    abstract fun updateInternalCache(old: Rectangle)

    override fun onBoundsChange(old: Rectangle) {
        super.onBoundsChange(old)
        updateInternalCache(old)
    }

    override fun isFinished(): Boolean {
        if (internal.isEmpty()) {
            return super.isFinished()
        }
        return internal.all { isFinished() }
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.pattern
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
        internal.forEach(remix::entityUpdate)
    }

    override fun onEnd() {
    }
}