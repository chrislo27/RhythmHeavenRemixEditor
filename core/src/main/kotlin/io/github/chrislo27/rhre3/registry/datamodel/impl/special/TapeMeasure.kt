package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.TapeMeasureEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class TapeMeasure(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 1f) {

    override fun createEntity(remix: Remix, cuePointer: CuePointer?): TapeMeasureEntity {
        return TapeMeasureEntity(remix, this)
    }

    override fun dispose() {
    }

}