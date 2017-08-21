package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeat(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  override val duration: Float,
                  override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel, DurationModel {

    val totalSequenceDuration: Float by lazy {
        val max = cues.maxBy(CuePointer::beat) ?: error("No cues in keep the beat")
        max.beat + max.duration
    }

    override fun createEntity(remix: Remix): KeepTheBeatEntity {
        return KeepTheBeatEntity(remix, this)
    }

    override fun dispose() {
    }

}