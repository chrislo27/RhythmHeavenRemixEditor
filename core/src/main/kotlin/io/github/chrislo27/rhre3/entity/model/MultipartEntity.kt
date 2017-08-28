package io.github.chrislo27.rhre3.entity.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.drawRect


abstract class MultipartEntity<out M>(remix: Remix, datamodel: M)
    : ModelEntity<M>(remix, datamodel), IRepitchable
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

    init {
        this.bounds.height = (1f +
                (datamodel.cues.maxBy(CuePointer::track)?.track ?: error("No cues in datamodel")))
                .coerceAtLeast(1f)
    }

    protected open fun translateInternal(oldBounds: Rectangle, changeWidths: Boolean = false) {
        internal.forEach {
            it.bounds.x = (it.bounds.x - oldBounds.x) + bounds.x
            it.bounds.y = (it.bounds.y - oldBounds.y) + bounds.y

            if (changeWidths) {
                it.bounds.width = (it.bounds.width / oldBounds.width) * bounds.width
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
            }

            override fun undo(context: Remix) {
                context.entities.removeAll(internalCache)
                context.entities.add(original)
            }

        }
    }

    fun canSplitWithoutColliding(): Boolean {
        return remix.entities.filter { it !== this && it !in internal }.none { target ->
            internal.any {
                it.bounds.overlaps(target.bounds)
            }
        }
    }

    override fun renderBeforeText(batch: SpriteBatch) {
        super.renderBeforeText(batch)
        if (shouldRenderInternal) {
            val batchColor = batch.color
            batch.setColor(batchColor.r, batchColor.g, batchColor.b, batchColor.a * 0.6f)
            internal.forEach {
                batch.drawRect(it.bounds.x, it.bounds.y,
                               it.bounds.width, it.bounds.height,
                               remix.editor.toScaleX(BORDER), remix.editor.toScaleY(BORDER))
            }
        }
    }

    abstract fun updateInternalCache(oldBounds: Rectangle)

    override fun onBoundsChange(old: Rectangle) {
        super.onBoundsChange(old)
        updateInternalCache(old)
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
        internal.forEach(remix::entityUpdate)
    }

    override fun onEnd() {
    }

    fun loadInternalSounds() {
        internal.filterIsInstance<CueEntity>().forEach { it.datamodel.loadSounds() }
    }

    fun unloadInternalSounds() {
        internal.filterIsInstance<CueEntity>().forEach { it.datamodel.unloadSounds() }
    }
}