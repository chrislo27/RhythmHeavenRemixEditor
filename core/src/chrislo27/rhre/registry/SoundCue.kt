package chrislo27.rhre.registry

import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.lazysound.LazySound
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.audio.Sound
import ionium.registry.AssetRegistry

data class SoundCue(val id: String, val fileExtension: String = "ogg", val name: String,
					val deprecated: List<String> = mutableListOf(), val duration: Float,
					val canAlterPitch: Boolean, val canAlterDuration: Boolean = false,
					val introSound: String? = null, val baseBpm: Float = 0f, val loops: Boolean = false,
					val soundFolder: String? = null) {

	var inspectionFunctions: List<InspectionFunction> = listOf()

	fun getLazySoundObj(): LazySound {
		return AssetRegistry.getAsset("soundCue_$id", LazySound::class.java)!!
	}

	fun getLazyIntroSoundObj(): LazySound? {
		if (introSound == null) return null
		return AssetRegistry.getAsset("soundCue_$introSound", LazySound::class.java)
	}

	fun getSoundObj(): Sound {
		return getLazySoundObj().sound
	}

	fun getIntroSoundObj(): Sound? {
		return getLazyIntroSoundObj()?.sound
	}

	fun attemptLoadSounds(): Boolean {
		val b: Boolean = !getLazySoundObj().isLoaded or (!(getLazyIntroSoundObj()?.isLoaded ?: false))

		getSoundObj()
		getIntroSoundObj()

		return b
	}

	fun shouldBeStopped() = canAlterDuration || loops

	fun shouldBeLooped() = (canAlterDuration && loops) || loops

	fun getPitch(semitone: Int, bpm: Float): Float {
		var result = 1f

		if (canAlterPitch)
			result = Semitones.getALPitch(semitone)

		if (baseBpm > 0) {
			result *= bpm / baseBpm
		}

		return result
	}

}

val SoundCue.canAlterPitch: Boolean
	get() {
		if (baseBpm > 0) return false
		return canAlterPitch
	}
