package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.lazysound.LazySound


open class Cue(game: Game, id: String, deprecatedIDs: List<String>, name: String,
               val duration: Float, val stretchable: Boolean, val repitchable: Boolean,
               val soundHandle: FileHandle,
               val introSound: String?, val endingSound: String?,
               val responseIDs: List<String>,
               val baseBpm: Float, val loops: Boolean)
    : Datamodel(game, id, deprecatedIDs, name) {

    val usesBaseBpm: Boolean
        get() = baseBpm > 0f

    val sound: LazySound by lazy {
        LazySound(soundHandle)
    }

    override val pickerName: String by lazy {
        "cue: $name"
    }
    val introSoundCue: Cue?
        get() =
            GameRegistry.data.objectMap[introSound] as Cue?
    val endingSoundCue: Cue?
        get() =
            GameRegistry.data.objectMap[endingSound] as Cue?

    fun getPitchForBaseBpm(bpm: Float): Float {
        if (baseBpm <= 0f)
            return 1f

        return bpm / baseBpm
    }

    override fun createEntity(remix: Remix): CueEntity {
        return CueEntity(remix, this)
    }

    override fun dispose() {
        sound.dispose()
    }
}