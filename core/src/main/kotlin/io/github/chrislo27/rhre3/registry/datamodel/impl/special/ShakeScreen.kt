package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.ShakeEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class ShakeScreen(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name) {

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): ShakeEntity {
        return ShakeEntity(remix, this)
    }

    override fun dispose() {
    }
}