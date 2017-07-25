package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.entity.ModelEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.toolboks.lazysound.LazySound


open class Cue(game: Game, id: String, deprecatedIDs: List<String>, name: String,
          val duration: Float, val stretchable: Boolean, val repitchable: Boolean,
          val soundHandle: FileHandle,
          val introSound: String?, val endingSound: String?,
          val responseIDs: List<String>)
    : Datamodel(game, id, deprecatedIDs, name) {

    val sound: LazySound by lazy {
        LazySound(soundHandle)
    }

    val introSoundCue: Cue?
        get() =
            GameRegistry.data.objectMap[introSound] as Cue?
    val endingSoundCue: Cue?
        get() =
            GameRegistry.data.objectMap[endingSound] as Cue?

    override fun createEntity(): ModelEntity<*> {
        TODO()
    }

    override fun dispose() {
        sound.dispose()
    }
}