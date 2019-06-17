package io.github.chrislo27.rhre3.sfxdb.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.ContainerModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.PreviewableModel
import io.github.chrislo27.rhre3.track.Remix


class Equidistant(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  duration: Float, val stretchable: Boolean,
                  override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name, duration), ContainerModel, PreviewableModel {

    override val canBePreviewed: Boolean by lazy { PreviewableModel.determineFromCuePointers(cues) }

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