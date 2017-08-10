package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.KeepTheBeat
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeatEntity(remix: Remix, datamodel: KeepTheBeat) : MultipartEntity<KeepTheBeat>(remix, datamodel) {
    override fun updateInternalCache(oldBounds: Rectangle) {
        TODO()
    }
}