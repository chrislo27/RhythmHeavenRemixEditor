package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.EndRemixEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class EndRemix(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 0.125f) {

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): EndRemixEntity {
        return EndRemixEntity(remix, this)
    }

    override fun dispose() {
    }
}
