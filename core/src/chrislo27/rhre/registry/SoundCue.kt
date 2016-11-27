package chrislo27.rhre.registry

import com.badlogic.gdx.audio.Sound
import ionium.registry.AssetRegistry
import ionium.util.AssetMap

data class SoundCue(val id: String, val fileExtension: String = "ogg", val name: String,
					val deprecated: List<String> = mutableListOf(), val duration: Float,
					val canAlterPitch: Boolean = false, val canAlterDuration: Boolean = false,
					val introSound: String? = null, val baseBpm: Float = 0f, val loops: Boolean = false) {

	fun getSoundObj(): Sound {
		return AssetRegistry.getSound(AssetMap.get("soundCue_$id"))
	}

	fun getIntroSoundObj(): Sound? {
		if (introSound == null) return null
		return AssetRegistry.getSound(AssetMap.get("soundCue_$introSound"))
	}

}
