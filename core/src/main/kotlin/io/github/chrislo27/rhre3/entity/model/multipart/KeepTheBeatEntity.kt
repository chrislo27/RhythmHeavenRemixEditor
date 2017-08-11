package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
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
        val sequenceLength: Float = datamodel.totalSequenceDuration
        val percentage: Float = bounds.width / sequenceLength
        val wholes: Int = percentage.toInt()
        val fractional: Float = percentage - percentage.toInt()

        // TODO optimize?
        internal.clear()

    }

    init {
        populate()
    }
}