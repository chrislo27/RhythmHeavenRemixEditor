package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.registry.datamodel.impl.KeepTheBeat
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeatEntity(remix: Remix, datamodel: KeepTheBeat)
    : MultipartEntity<KeepTheBeat>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = true

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds)
        val widthChanged = oldBounds.width != bounds.width

        if (widthChanged) {
            populate()
        }
    }

    private fun populate() {
        val cues = datamodel.cues
        if (cues.isEmpty())
            error("KeepTheBeat datamodel has nothing in it")

        val sequenceLength: Float = datamodel.totalSequenceDuration
        val percentage: Float = bounds.width / sequenceLength
        val wholes: Int = percentage.toInt()
        val fractional: Float = percentage - percentage.toInt()

        // TODO optimize?
        internal.clear()
        for (index in 0..(percentage * cues.size).toInt()) {
            val cycle = index / cues.size
            val remIndex: Int = index % cues.size
            val pointer: CuePointer = cues[remIndex]
            val beat = pointer.beat + cycle * sequenceLength

            internal += GameRegistry.data.objectMap[pointer.id]?.createEntity(remix)?.apply {
                this.bounds.x = this@KeepTheBeatEntity.bounds.x + beat
                this.bounds.y = this@KeepTheBeatEntity.bounds.y + pointer.track
                this.bounds.width = pointer.duration

                (this as? IRepitchable)?.semitone = pointer.semitone
            } ?: error("Missing object ${pointer.id} while trying to populate keep the beat ${datamodel.id}")
        }
    }

    init {
        this.bounds.width = datamodel.duration
        populate()
    }

    override fun copy(remix: Remix): KeepTheBeatEntity {
        return KeepTheBeatEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@KeepTheBeatEntity.bounds)
            }
            it.semitone = this.semitone
        }
    }
}