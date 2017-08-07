package io.github.chrislo27.rhre3.registry.datamodel

import io.github.chrislo27.rhre3.entity.model.ModelEntity


class Equidistant(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  val distance: Float, val stretchable: Boolean,
                  val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name) {

    override fun createEntity(): ModelEntity<*> {
        TODO()
    }

    override fun dispose() {
    }

}