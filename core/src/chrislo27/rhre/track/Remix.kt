package chrislo27.rhre.track

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import com.badlogic.gdx.audio.Music

class Remix {

	val entities: MutableList<Entity> = mutableListOf()
	val selection: List<Entity> = mutableListOf()
	val tempoChanges: TempoChanges = TempoChanges(120f)

	private var playingState = PlayingState.STOPPED
	@Volatile
	var music: Music? = null

	private var beat: Float = 0f
	var musicStartTime: Float = 0f
	var playbackStart: Float = 0f
	private var endTime: Float = 0f

	init {

	}

	fun setPlayingState(ps: PlayingState): Unit {
		fun resetEntitiesAndTracker(): Unit {
			// reset playback completion
			entities.forEach(Entity::reset)
			beat = playbackStart
			entities.forEach {
				if (it is PatternEntity) {
					it.internal.filter { inter -> it.bounds.x + inter.bounds.x < beat }.forEach { inter ->
						inter.playbackCompletion = PlaybackCompletion.FINISHED
						inter.onEnd(0f)
					}
				} else if (it.bounds.x + it.bounds.width < beat) {
					it.playbackCompletion = PlaybackCompletion.FINISHED
					it.onEnd(0f)
				}
			}
		}

		when (playingState) {
			PlayingState.PLAYING -> {
			}
			PlayingState.PAUSED -> {
			}
			PlayingState.STOPPED -> {
				resetEntitiesAndTracker()
			}
		}

		playingState = ps

		// change to
		when (ps) {
			PlayingState.PLAYING -> {
				var length = Float.MIN_VALUE
				entities.forEach { length = Math.max(length, it.bounds.x + it.bounds.width) }

				endTime = length
			}
			PlayingState.PAUSED -> {
			}
			PlayingState.STOPPED -> {
				resetEntitiesAndTracker()
			}
		}
	}

	fun getPlayingState(): PlayingState = playingState

	fun getBeat(): Float = beat

	fun update(delta: Float): Unit {
		if (playingState != PlayingState.PLAYING)
			return@update

		// we rely on music being our timekeeper,
		// but delta time is required since music position only updates so frequently
		// if the internal beat is more than the music reported time, set it to the music time
		// otherwise increment by delta

		val musicInBeats: Float =
				if (music != null)
					tempoChanges.secondsToBeats(music!!.position + musicStartTime)
				else
					beat

		if (music != null && music!!.isPlaying && beat >= musicInBeats) {
			beat = musicInBeats
		} else {
			beat = tempoChanges.secondsToBeats(tempoChanges.beatsToSeconds(beat) + delta)
		}

		for (e: Entity in entities) {
			if (e.playbackCompletion == PlaybackCompletion.FINISHED) continue

			if (beat >= e.bounds.x) {
				if (e.playbackCompletion == PlaybackCompletion.WAITING) {
					e.onStart(delta)
					e.playbackCompletion = PlaybackCompletion.STARTED
				}

				if (e.playbackCompletion == PlaybackCompletion.STARTED) {
					e.onWhile(delta)

					if (beat >= e.bounds.x + e.bounds.width) {
						e.onEnd(delta)
						e.playbackCompletion = PlaybackCompletion.FINISHED
					}
				}
			}
		}

		if (beat >= endTime)
			setPlayingState(PlayingState.STOPPED)
	}

}

enum class PlayingState {
	PLAYING, PAUSED, STOPPED;
}

enum class PlaybackCompletion {
	WAITING, STARTED, FINISHED;
}
