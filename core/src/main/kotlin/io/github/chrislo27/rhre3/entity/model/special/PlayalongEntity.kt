package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.PlayalongModel
import io.github.chrislo27.rhre3.track.Remix


class PlayalongEntity(remix: Remix, datamodel: PlayalongModel)
    : ModelEntity<PlayalongModel>(remix, datamodel), IRepitchable, IStretchable {

    override var semitone: Int
        get() = PlayalongInput.reverseIndexOf(playalongInput)
        set(value) {
            playalongInput = PlayalongInput.VALUES.reversed().getOrNull(value)
        }
    override val canBeRepitched: Boolean = true
    override val rangeWrapsAround: Boolean = true
    override val persistSemitoneData: Boolean = false
    override val showPitchOnTooltip: Boolean = false
    override val semitoneRange: IntRange get() = PlayalongInput.NUMBER_RANGE
    override val isStretchable: Boolean = datamodel.stretchable

    var playalongInput: PlayalongInput? = PlayalongInput.BUTTON_A

    init {
        this.bounds.width = 0.5f
        this.bounds.height = 1f
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.cue
    }

    override fun getTextForSemitone(semitone: Int): String {
        return playalongInput?.displayText ?: "[RED]INVALID[]"
    }

    override fun copy(remix: Remix): PlayalongEntity {
        return PlayalongEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@PlayalongEntity.bounds)
                it.playalongInput = this@PlayalongEntity.playalongInput
            }
        }
    }

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)
        objectNode.put("playalongInput", playalongInput?.id)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        playalongInput = PlayalongInput[objectNode["playalongInput"]?.asText("???") ?: "???"]
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

}