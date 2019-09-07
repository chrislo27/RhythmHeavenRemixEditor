package io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.TextureEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class TextureModel(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 1f) {
    
    final override val hideInPresentationMode: Boolean = false

    override fun createEntity(remix: Remix, cuePointer: CuePointer?): TextureEntity {
        return TextureEntity(remix, this)
    }

    override fun dispose() {
    }

}