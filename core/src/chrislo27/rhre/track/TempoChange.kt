package chrislo27.rhre.track

import com.badlogic.gdx.utils.IntMap
import ionium.util.Utils
import java.util.*


data class TempoChange constructor(val beat: Float, val tempo: Float, private val tc: TempoChanges) {

	val seconds: Float = tc.beatsToSeconds(beat)

}

class TempoChanges(val defTempo: Float = 120f) {

	private val beatMap: NavigableMap<Float, TempoChange> = TreeMap()
	private val secondsMap: NavigableMap<Float, TempoChange> = TreeMap()

	init {

	}

	fun remove(tc: TempoChange) {
		beatMap.remove(tc.beat)
		secondsMap.remove(tc.seconds)

		update()
	}

	fun add(tc: TempoChange) {
		beatMap.put(tc.beat, tc)
		secondsMap.put(tc.seconds, tc)

		update()
	}

	fun update() {
		val oldBeats: MutableList<TempoChange> = mutableListOf()

		beatMap.entries.forEach { oldBeats.add(it.value) }

		beatMap.clear()
		secondsMap.clear()

		oldBeats.forEach {
			val tc = TempoChange(it.beat, it.tempo, this)

			beatMap.put(tc.beat, tc)
			secondsMap.put(tc.seconds, tc)
		}
	}

	fun getCount(): Int = Math.min(beatMap.size, secondsMap.size)

	fun getBeatMap() = beatMap
	fun getSecondsMap() = secondsMap

	fun getTempoChangeFromBeat(beat: Float): TempoChange? = beatMap.lowerEntry(beat)?.value

	fun getTempoChangeFromSecond(second: Float): TempoChange? = secondsMap.lowerEntry(second)?.value

	fun beatsToSeconds(beat: Float): Float {
		val tc: TempoChange = getTempoChangeFromBeat(beat) ?: return BpmUtils.beatsToSeconds(beat, defTempo)

		return tc.seconds + BpmUtils.beatsToSeconds(beat - tc.beat, tc.tempo)
	}

	fun secondsToBeats(seconds: Float): Float {
		val tc: TempoChange = getTempoChangeFromSecond(seconds) ?: return BpmUtils.secondsToBeats(seconds, defTempo)

		return tc.beat + BpmUtils.secondsToBeats(seconds - tc.seconds, tc.tempo)
	}

}

object BpmUtils {
	fun beatsToSeconds(beat: Float, bpm: Float): Float = beat / (bpm / 60)

	fun secondsToBeats(seconds: Float, bpm: Float): Float = seconds * (bpm / 60)
}

object Semitones {

	const val SEMITONES_IN_OCTAVE = 12
	const val SEMITONE_VALUE = 1f / SEMITONES_IN_OCTAVE

	private val cachedPitches = IntMap<Float>()

	@JvmStatic
	fun getSemitoneName(semitone: Int): String {
		var thing: String

		when (Math.abs(Math.floorMod(semitone, SEMITONES_IN_OCTAVE))) {
			0 -> thing = "C"
			1 -> thing = "C#"
			2 -> thing = "D"
			3 -> thing = "D#"
			4 -> thing = "E"
			5 -> thing = "F"
			6 -> thing = "F#"
			7 -> thing = "G"
			8 -> thing = "G#"
			9 -> thing = "A"
			10 -> thing = "A#"
			11 -> thing = "B"
			else -> thing = "N/A"
		}

		if (semitone >= 12 || semitone <= -1) {
			thing += Utils.repeat(if (semitone > 0) "+" else "-",
								  Math.abs(Math.floorDiv(semitone, SEMITONES_IN_OCTAVE)))
		}

		return thing
	}

	@JvmStatic
	fun getALPitch(semitone: Int): Float {
		if (cachedPitches.get(semitone) == null) {
			cachedPitches.put(semitone, Math.pow(2.0, (semitone * SEMITONE_VALUE).toDouble()).toFloat())
		}

		return cachedPitches.get(semitone)
	}

}