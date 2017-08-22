package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.EndEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix


class EndRemix(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : Datamodel(game, id, deprecatedIDs, name) {

    init {
        if (game.id != GameRegistry.SPECIAL_GAME_ID)
            error("EndRemix created outside of the special game")
    }

    override fun createEntity(remix: Remix): EndEntity {
        return EndEntity(remix, this)
    }

    override fun dispose() {
    }
}
