package io.github.chrislo27.rhre3.registry.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix


class Subtitle(game: Game, id: String, deprecatedIDs: List<String>, name: String)
    : SpecialDatamodel(game, id, deprecatedIDs, name) {

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): SubtitleEntity {
        return SubtitleEntity(remix, this).apply {
            if (cuePointer != null) {
                val text = cuePointer.metadata["subtitleText"] as? String? ?: ""
                this.subtitle = text
            }
        }
    }

    override fun dispose() {
    }
}
