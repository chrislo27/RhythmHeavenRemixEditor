package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.PlayalongModel
import io.github.chrislo27.rhre3.track.Remix


class PlayalongEntity(remix: Remix, datamodel: PlayalongModel)
    : ModelEntity<PlayalongModel>(remix, datamodel) {

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.cue
    }

    override fun copy(remix: Remix): PlayalongEntity {
        return PlayalongEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@PlayalongEntity.bounds)
            }
        }
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

}