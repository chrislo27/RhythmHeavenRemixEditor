package chrislo27.rhre.registry

import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.audio.Sound
import ionium.registry.AssetRegistry
import ionium.registry.lazysound.LazySound
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua

data class SoundCue(val id: String, val gameID: String, val fileExtension: String = "ogg", val name: String,
					val deprecated: List<String> = mutableListOf(), val duration: Float,
					val canAlterPitch: Boolean, val canAlterDuration: Boolean = false,
					val introSound: String? = null, val baseBpm: Float = 0f, val loops: Boolean = false,
					val soundFolder: String? = null) {

	var inspectionFunctions: List<InspectionFunction> = listOf()

	val luaValue: LuaValue by lazy {
		val l = LuaValue.tableOf()

		l.set("id", id)
		l.set("gameID", gameID)
		l.set("name", name)
		l.set("duration", duration.toDouble())
		l.set("deprecated", LuaValue.listOf(deprecated.map { CoerceJavaToLua.coerce(it) }.toTypedArray()))
		l.set("canAlterPitch", CoerceJavaToLua.coerce(canAlterPitch))
		l.set("canAlterDuration", CoerceJavaToLua.coerce(canAlterDuration))
		l.set("introSound", CoerceJavaToLua.coerce(introSound))
		l.set("baseBpm", baseBpm.toDouble())
		l.set("loops", CoerceJavaToLua.coerce(loops))

		return@lazy l
	}

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

	fun needsToBeLoaded(): Boolean {
		return !getLazySoundObj().isLoaded or (!(getLazyIntroSoundObj()?.isLoaded ?: false))
	}

	fun attemptLoadSounds(): Boolean {
		val b: Boolean = needsToBeLoaded()

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
