package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.PitchBenderModel
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class PitchBenderEntity(remix: Remix, datamodel: PitchBenderModel)
    : ModelEntity<PitchBenderModel>(remix, datamodel), IStretchable, IRepitchable {

    override var semitone: Int = 0
    override val canBeRepitched: Boolean = true
    override val isStretchable: Boolean = true
    
    override fun getTextForSemitone(semitone: Int): String {
        return "${if (semitone > 0) "+" else ""}$semitone"
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.cue
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun copy(remix: Remix): PitchBenderEntity {
        return PitchBenderEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this.bounds)
            }
            it.semitone = this.semitone
        }
    }


}