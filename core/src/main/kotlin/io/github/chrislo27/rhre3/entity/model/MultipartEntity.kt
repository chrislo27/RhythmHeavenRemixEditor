package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.intersects


abstract class MultipartEntity<out M>(remix: Remix, datamodel: M)
    : ModelEntity<M>(remix, datamodel), IRepitchable, ILoadsSounds
        where M : Datamodel, M : ContainerModel {

    override var semitone: Int = 0
        set(value) {
            val change = value - field
            field = value

            internal.filterIsInstance<IRepitchable>()
                    .filter(IRepitchable::canBeRepitched)
                    .forEach { it.semitone += change }
        }
    override val canBeRepitched: Boolean by IRepitchable.anyInModel(datamodel)

    override var playbackCompletion: PlaybackCompletion = super.playbackCompletion
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
    open val shouldRenderInternal: Boolean = true
    protected var internalWidth: Float = computeInternalWidth()

    init {
        this.bounds.height = (1f +
                (datamodel.cues.maxBy(CuePointer::track)?.track ?: error("No cues in datamodel")))
                .coerceAtLeast(1f)
    }

    open fun getInternalEntities(): List<Entity> {
        return internal
    }

    protected fun computeInternalWidth(): Float {
        return internal.maxBy { it.bounds.x + it.bounds.width }?.run {
            this.bounds.x + this.bounds.width - this@MultipartEntity.bounds.x
        } ?: bounds.width
    }

    override fun inRenderRange(start: Float, end: Float): Boolean {
        return bounds.x + lerpDifference.x + internalWidth >= start && bounds.x + lerpDifference.x <= end
    }

    override fun getUpperUpdateableBound(): Float {
        return bounds.x + internalWidth
    }

    protected open fun translateInternal(oldBounds: Rectangle, changeWidths: Boolean = false,
                                         scaleBeats: Boolean = false) {
        internal.forEach {
            it.updateBounds {
                if (!scaleBeats) {
                    it.bounds.x = (it.bounds.x - oldBounds.x) + bounds.x
                } else {
                    it.bounds.x = ((it.bounds.x - oldBounds.x) / oldBounds.width) * bounds.width + bounds.x
                }
                it.bounds.y = (it.bounds.y - oldBounds.y) + bounds.y

                if (changeWidths) {
                    it.bounds.width = (it.bounds.width / oldBounds.width) * bounds.width
                }
            }
        }
    }

    fun createSplittingAction(): ReversibleAction<Remix> {
        return object : ReversibleAction<Remix> {

            val original = this@MultipartEntity
            val internalCache = internal.toList()

            override fun redo(context: Remix) {
                context.entities.remove(original)
                context.entities.addAll(internalCache)
                internalCache.forEach {
                    it.updateInterpolation(true)
                }
            }

            override fun undo(context: Remix) {
                context.entities.removeAll(internalCache)
                context.entities.add(original)
                original.updateInterpolation(true)
            }

        }
    }

    fun canSplitWithoutColliding(): Boolean {
        return remix.entities.filter { it !== this && it !in internal }.none { target ->
            internal.any {
                it.bounds.intersects(target.bounds)
            }
        }
    }

    override fun renderBeforeText(batch: SpriteBatch) {
        super.renderBeforeText(batch)
        if (shouldRenderInternal) {
            val batchColor = batch.color
            batch.setColor(batchColor.r, batchColor.g, batchColor.b, batchColor.a * 0.6f)
            internal.forEach {
                batch.drawRect(it.bounds.x + lerpDifference.x, it.bounds.y + lerpDifference.y,
                               it.bounds.width, it.bounds.height,
                               remix.editor.toScaleX(BORDER), remix.editor.toScaleY(BORDER))
            }
        }
    }

    abstract fun updateInternalCache(oldBounds: Rectangle)

    override fun onBoundsChange(old: Rectangle) {
        super.onBoundsChange(old)
        updateInternalCache(old)
        internalWidth = computeInternalWidth()
    }

    override fun isFinished(): Boolean {
        if (internal.isEmpty()) {
            return super.isFinished()
        }
        return internal.all(Entity::isFinished)
    }

    override fun onStart() {
        internal.filter {
            it.bounds.x + it.bounds.width < remix.beat
        }.forEach {
                    it.playbackCompletion = PlaybackCompletion.FINISHED
                }
    }

    override fun whilePlaying() {
        internal.forEach { remix.entityUpdate(it) }
    }

    override fun onEnd() {
    }

    override fun loadSounds() {
        internal.filterIsInstance<ILoadsSounds>().forEach(ILoadsSounds::loadSounds)
    }

    override fun unloadSounds() {
        internal.filterIsInstance<ILoadsSounds>().forEach(ILoadsSounds::unloadSounds)
    }
}