package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.KeepTheBeat
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeatEntity(remix: Remix, datamodel: KeepTheBeat)
    : MultipartEntity<KeepTheBeat>(remix, datamodel), IStretchable, IRepitchable {

    override val isStretchable: Boolean = true
    override var semitone: Int = 0
    override val canBeRepitched: Boolean by IRepitchable.anyInModel(datamodel)

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds)
        val widthChanged = oldBounds.width != bounds.width

        TODO()
    }

    init {
        datamodel.cues.mapTo(internal) { pointer ->
            GameRegistry.data.objectMap[pointer.id]?.createEntity(remix)?.apply {
                this.bounds.x = this@KeepTheBeatEntity.bounds.x + pointer.beat
                this.bounds.y = this@KeepTheBeatEntity.bounds.y + pointer.track
                this.bounds.width = pointer.duration

                // apply cue pointer settings
                (this as? IRepitchable)?.semitone = pointer.semitone
            } ?: error("Object with id ${pointer.id} not found")
        }
    }
}