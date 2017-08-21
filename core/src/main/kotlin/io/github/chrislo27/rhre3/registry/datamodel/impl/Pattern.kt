package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.PatternEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.track.Remix

class Pattern(game: Game, id: String, deprecatedIDs: List<String>, name: String,
              override val cues: List<CuePointer>, val stretchable: Boolean)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel, DurationModel {

    val repitchable: Boolean by lazy {
        cues.any {
            (GameRegistry.data.objectMap[it.id] as? Cue)?.repitchable == true
        }
    }

    override val duration: Float by lazy {
        cues.map { it.duration }.sum()
    }

    override fun createEntity(remix: Remix): PatternEntity {
        return PatternEntity(remix, this)
    }

    override fun dispose() {
    }

}
