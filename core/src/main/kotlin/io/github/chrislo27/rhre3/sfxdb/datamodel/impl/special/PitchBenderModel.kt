package io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.PitchBenderEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.PickerName
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class PitchBenderModel(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 1f) {

    override val pickerName: PickerName = super.pickerName.copy(sub = "[LIGHT_GRAY]Affects Rockers's pitch bending cues[]")
    override val hideInPresentationMode: Boolean = false

    override fun createEntity(remix: Remix, cuePointer: CuePointer?): PitchBenderEntity {
        return PitchBenderEntity(remix, this)
    }

    override fun dispose() {
    }
}