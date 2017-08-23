package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


abstract class SpecialDatamodel(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : Datamodel(game, id, deprecatedIDs, name) {

    fun checkGameValidity() {
        if (game.id != GameRegistry.SPECIAL_GAME_ID)
            error("Special datamodel (${this::class.simpleName}) created outside of the special game")
    }

    init {
        checkGameValidity()
    }

}