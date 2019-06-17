package io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.MusicDistortEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.PickerName
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class MusicDistortModel(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 1f) {

    override val pickerName: PickerName = super.pickerName.copy(sub = "[LIGHT_GRAY]Applies bandpass filter on music[]")

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): MusicDistortEntity {
        return MusicDistortEntity(remix, this)
    }

    override fun dispose() {
    }
}