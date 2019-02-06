package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.playalong.PlayalongMethod
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.PlayalongModel
import io.github.chrislo27.rhre3.track.Remix


class PlayalongEntity(remix: Remix, datamodel: PlayalongModel)
    : ModelEntity<PlayalongModel>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = datamodel.stretchable

    val playalongInput: PlayalongInput get() = datamodel.playalongInput
    val playalongMethod: PlayalongMethod get() = datamodel.playalongMethod
    override var needsNameTooltip: Boolean
        get() = false
        set(_) {}

    init {
        this.bounds.width = if (playalongMethod == PlayalongMethod.PRESS) 0.5f else 1f
        this.bounds.height = 1f
    }

    override fun getHoverText(inSelection: Boolean): String? {
        return datamodel.pickerName.main
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.cue
    }

    override fun getTextForSemitone(semitone: Int): String {
        return playalongInput.displayText
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