package io.github.chrislo27.rhre3.sfxdb.datamodel.impl

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.sfxdb.BaseBpmRules
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.PickerName
import io.github.chrislo27.rhre3.sfxdb.datamodel.PreviewableModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel
import io.github.chrislo27.rhre3.soundsystem.AudioPointer
import io.github.chrislo27.rhre3.soundsystem.SoundCache
import io.github.chrislo27.rhre3.track.Remix
import java.io.File


open class Cue(game: Game, id: String, deprecatedIDs: List<String>, name: String,
               duration: Float, val stretchable: Boolean, val repitchable: Boolean,
               val soundHandle: FileHandle,
               val introSound: String?, val endingSound: String?,
               override val responseIDs: List<String>,
               val baseBpm: Float, val useTimeStretching: Boolean, val baseBpmRules: BaseBpmRules,
               val loops: Boolean, val earliness: Float,
               val loopStart: Float, val loopEnd: Float,
               val pitchBending: Boolean, val writtenPitch: Int)
    : Datamodel(game, id, deprecatedIDs, name, duration), ResponseModel, PreviewableModel {

    val usesBaseBpm: Boolean
        get() = baseBpm > 0f
    
    val soundFile: File = soundHandle.file()

    val sound: AudioPointer get() = SoundCache.getOrLoad(soundFile)

    val introSoundCue: Cue?
        get() =
            SFXDatabase.data.objectMap[introSound] as Cue?
    val endingSoundCue: Cue?
        get() =
            SFXDatabase.data.objectMap[endingSound] as Cue?

    override val pickerName: PickerName
        get() = if (id != SFXDatabase.SKILL_STAR_ID) super.pickerName else super.pickerName.copy(sub = "[LIGHT_GRAY](usable in Playalong)[]")

    init {
        if (!soundHandle.exists()) {
            error("Sound handle for $id doesn't exist")
        }
    }

    fun loadSounds() {
        sound
        introSoundCue?.loadSounds()
        endingSoundCue?.loadSounds()
    }

    fun unloadSounds() {
        SoundCache.unload(soundFile)
        introSoundCue?.unloadSounds()
        endingSoundCue?.unloadSounds()
    }

    fun getAdjustedRateForBaseBpm(bpm: Float): Float {
        if (baseBpm <= 0f)
            return 1f

        return (bpm / baseBpm)
    }

    override fun createEntity(remix: Remix,
                              cuePointer: CuePointer?): CueEntity {
        return CueEntity(remix, this).apply {
            if (cuePointer != null) {
                semitone = cuePointer.semitone
                volumePercent = cuePointer.volume
            }
        }
    }

    override fun dispose() {
        unloadSounds()
    }
}