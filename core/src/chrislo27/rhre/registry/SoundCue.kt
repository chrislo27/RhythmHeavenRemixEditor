package chrislo27.rhre.registry

import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.audio.Sound
import ionium.registry.AssetRegistry

data class SoundCue(val id: String, val fileExtension: String = "ogg", val name: String,
					val deprecated: List<String> = mutableListOf(), val duration: Float,
					val canAlterPitch: Boolean = false, val canAlterDuration: Boolean = false,
					val introSound: String? = null, val baseBpm: Float = 0f, val loops: Boolean = false,
					val soundFolder: String? = null) {

	fun getSoundObj(): Sound {
		return AssetRegistry.getSound("soundCue_$id")
	}

	fun getIntroSoundObj(): Sound? {
		if (introSound == null) return null
		return AssetRegistry.getSound("soundCue_$introSound")
	}

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
