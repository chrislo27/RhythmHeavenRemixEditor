package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.PlayalongEntity
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.PickerName
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.registry.datamodel.toPickerName
import io.github.chrislo27.rhre3.track.Remix


class PlayalongModel(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                     val stretchable: Boolean, override val pickerName: PickerName = name.toPickerName())
    : SpecialDatamodel(game, id, deprecatedIDs, "Playalong - $name") {

    override fun createEntity(remix: Remix, cuePointer: CuePointer?): PlayalongEntity {
        return PlayalongEntity(remix, this).also {
            if (cuePointer != null) {
                val i = cuePointer.metadata["playalongInput"] as? String?
                if (i != null) {
                    it.playalongInput = PlayalongInput[i]
                }
            }
        }
    }

    override fun dispose() {
    }
}