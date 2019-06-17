package io.github.chrislo27.rhre3.sfxdb.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.ContainerModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.PreviewableModel
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeat(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  duration: Float, override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name, duration), ContainerModel, PreviewableModel {

    override val canBePreviewed: Boolean by lazy { PreviewableModel.determineFromCuePointers(cues) }

    val totalSequenceDuration: Float by lazy {
        val max = cues.maxBy(CuePointer::beat) ?: error("No cues in keep the beat")
        max.beat + max.duration
    }

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): KeepTheBeatEntity {
        return KeepTheBeatEntity(remix, this).apply {
            if (cuePointer != null) {
                semitone = cuePointer.semitone
                volumePercent = cuePointer.volume
            }
        }
    }

    override fun dispose() {
    }

}