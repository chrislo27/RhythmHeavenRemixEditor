package io.github.chrislo27.rhre3.registry.datamodel.impl

import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix


class KeepTheBeat(game: Game, id: String, deprecatedIDs: List<String>, name: String,
                  val defaultDuration: Float,
                  override val cues: List<CuePointer>)
    : Datamodel(game, id, deprecatedIDs, name), ContainerModel {

    val totalSequenceDuration: Float by lazy {
        cues.maxBy(CuePointer::beat)?.beat ?: error("No cues in keep the beat")
    }

    override fun createEntity(remix: Remix): ModelEntity<*> {
        TODO()
    }

    override fun dispose() {
    }

}