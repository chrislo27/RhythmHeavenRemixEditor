package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.cue.RandomCueEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix

class RandomCue(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel {

    override fun createEntity(remix: Remix): RandomCueEntity {
        return RandomCueEntity(remix, this)
    }

    override fun dispose() {
    }

}
