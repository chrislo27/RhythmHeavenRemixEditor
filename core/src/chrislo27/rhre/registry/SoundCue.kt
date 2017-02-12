package chrislo27.rhre.registry

import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import ionium.registry.AssetRegistry

data class SoundCue(val id: String, val fileExtension: String = "ogg", val name: String,
					val deprecated: List<String> = mutableListOf(), val duration: Float,
					val canAlterPitch: Boolean, val canAlterDuration: Boolean = false,
					val introSound: String? = null, val baseBpm: Float = 0f, val loops: Boolean = false,
					val soundFolder: String? = null) {

	lateinit var alMusic: OpenALMusic
		private set
	var inspectionFunctions: List<InspectionFunction> = listOf()

	fun getSoundObj(): Sound {
		return AssetRegistry.getSound("soundCue_$id")
	}

	fun getIntroSoundObj(): Sound? {
		if (introSound == null) return null
		return AssetRegistry.getSound("soundCue_$introSound")
	}

	fun loadALMusic(path: String) {
		alMusic = Gdx.audio.newMusic(Gdx.files.local(path)) as OpenALMusic
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
