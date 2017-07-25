package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.files.FileHandle


class TempoBasedCue(game: Game, id: String, deprecatedIDs: List<String>, name: String, duration: Float,
                    stretchable: Boolean, repitchable: Boolean, soundHandle: FileHandle, introSound: String?,
                    endingSound: String?, responseIDs: List<String>,
                    val baseBpm: Float)
    : Cue(game, id, deprecatedIDs, name, duration,
          stretchable, repitchable, soundHandle,
          introSound, endingSound, responseIDs) {
}