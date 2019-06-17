package io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special

import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.Toolboks


class Subtitle(game: Game, id: String, deprecatedIDs: List<String>, name: String, type: String?)
    : SpecialDatamodel(game, id, deprecatedIDs, name, 1f) {

    val type: SubtitleType = when (type) {
        null -> SubtitleType.SUBTITLE
        else -> {
            SubtitleType.VALUES.firstOrNull { it.metadata == type } ?: run {
                Toolboks.LOGGER.warn("Unknown subtitle type: $type")
                SubtitleType.SUBTITLE
            }
        }
    }

    enum class SubtitleType(val metadata: String, val canInputNewlines: Boolean) {
        SUBTITLE("subtitle", true), SONG_TITLE("songTitle", false), SONG_ARTIST("songArtist", false);

        companion object {
            val VALUES = values()
        }
    }

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
