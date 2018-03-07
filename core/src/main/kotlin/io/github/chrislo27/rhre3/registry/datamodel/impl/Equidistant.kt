package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.track.Remix


class Equidistant(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  override val duration: Float, val stretchable: Boolean,
                  override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel, DurationModel {

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): EquidistantEntity {
        return EquidistantEntity(remix, this).apply {
            if (cuePointer != null) {
                semitone = cuePointer.semitone
                volumePercent = cuePointer.volume
            }
        }
    }

    override fun dispose() {
    }

}