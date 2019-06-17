package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.MusicDistortModel
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class MusicDistortEntity(remix: Remix, datamodel: MusicDistortModel)
    : ModelEntity<MusicDistortModel>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = true

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.cue
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun copy(remix: Remix): MusicDistortEntity {
        return MusicDistortEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this.bounds)
            }
        }
    }


}