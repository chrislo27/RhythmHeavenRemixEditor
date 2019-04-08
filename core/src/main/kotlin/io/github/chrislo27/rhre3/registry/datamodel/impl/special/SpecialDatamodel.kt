package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


abstract class SpecialDatamodel(game: Game, id: String, deprecatedIDs: List<String>, name: String, duration: Float)
    : Datamodel(game, id, deprecatedIDs, name, duration) {

    protected open fun gameValidityPredicate(game: Game): Boolean {
        return game.isSpecial
    }

    fun checkGameValidity() {
        if (!gameValidityPredicate(game))
            error("Special datamodel (${this::class.simpleName}) created not in a special-marked game ${game.id}")
    }

    init {
        checkGameValidity()
    }

}