package chrislo27.rhre.track

import java.util.*

data class TempoChange constructor (val beat: Float, val tempo: Float, private val tc: TempoChanges,
									val unremoveable: Boolean = false) {

	val seconds: Float by lazy {
		if (tc.getCount() == 0)
			0f
		else {
			tc.beatsToSeconds(beat)
		}
	}

}

class TempoChanges(defTempo: Float) {

	private val beatMap: NavigableMap<Float, TempoChange> = TreeMap<Float, TempoChange>()
	private val secondsMap: NavigableMap<Float, TempoChange> = TreeMap<Float, TempoChange>()

	init {
		add(TempoChange(0f, defTempo, this, unremoveable = true))
	}

	fun remove(tc: TempoChange) {
		if (getCount() <= 1) return
		if (tc.unremoveable) return

		beatMap.remove(tc.beat)
		secondsMap.remove(tc.seconds)
	}

	fun add(tc: TempoChange) {
		beatMap.put(tc.beat, tc)
		secondsMap.put(tc.seconds, tc)
	}

	fun getCount(): Int = Math.min(beatMap.size, secondsMap.size)

	fun getTempoChangeFromBeat(beat: Float): TempoChange = beatMap.lowerEntry(beat)?.value ?: beatMap.firstEntry()!!.value

	fun getTempoChangeFromSecond(second: Float): TempoChange = secondsMap.lowerEntry(second)?.value ?: secondsMap.firstEntry()!!.value

	fun beatsToSeconds(beat: Float): Float {
		val tc: TempoChange = getTempoChangeFromBeat(beat)

		return tc.seconds + BpmUtils.beatsToSeconds(beat - tc.beat, tc.tempo)
	}

	fun secondsToBeats(seconds: Float): Float {
		val tc: TempoChange = getTempoChangeFromSecond(seconds)

		return tc.beat + BpmUtils.secondsToBeats(seconds - tc.seconds, tc.tempo)
	}

}

object BpmUtils {
	fun beatsToSeconds(beat: Float, bpm: Float): Float = beat / (bpm / 60)

	fun secondsToBeats(seconds: Float, bpm: Float): Float = seconds * (bpm / 60)
}
