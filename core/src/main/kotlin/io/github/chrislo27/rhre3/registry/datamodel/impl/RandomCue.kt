package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.RandomCueEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import io.github.chrislo27.rhre3.track.Remix

class RandomCue(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                override val cues: List<CuePointer>, override val responseIDs: List<String>)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel, ResponseModel {

    override val duration: Float by lazy {
        cues.maxBy(CuePointer::duration)?.duration ?: error("No cues found")
    }

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): RandomCueEntity {
        return RandomCueEntity(remix, this).apply {
            if (cuePointer != null) {
                semitone = cuePointer.semitone
                volumePercent = cuePointer.volume
            }
        }
    }

    override fun dispose() {
    }

}
