package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.Equidistant
import io.github.chrislo27.rhre3.track.Remix


class EquidistantEntity(remix: Remix, datamodel: Equidistant) : MultipartEntity<Equidistant>(remix, datamodel) {
    override fun updateInternalCache(old: Rectangle) {
        TODO()
    }
}