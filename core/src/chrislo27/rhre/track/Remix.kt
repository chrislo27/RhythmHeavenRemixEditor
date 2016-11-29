package chrislo27.rhre.track

import chrislo27.rhre.entity.Entity
import com.badlogic.gdx.audio.Music

class Remix {

	val entities: MutableList<Entity> = mutableListOf()
	val selection: List<Entity> = mutableListOf()
	val tempoChanges: TempoChanges = TempoChanges(120f)

	private var playingState = PlayingState.STOPPED
	@Volatile
	var music: Music? = null

	private var beat: Float = 0f
	private var musicStartTime: Float = 0f

	init {

	}

	fun setPlayingState(ps: PlayingState): Unit {
		when (playingState) {
			PlayingState.PLAYING -> {
			}
			PlayingState.PAUSED -> {
			}
			PlayingState.STOPPED -> {
			}
		}

		playingState = ps

		// change to
		when (ps) {
			PlayingState.PLAYING -> {
			}
			PlayingState.PAUSED -> {
			}
			PlayingState.STOPPED -> {
				// reset playback completion
				entities.forEach(Entity::reset)

				// FIXME
				beat = 0f
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
	}

}

enum class PlayingState {
	PLAYING, PAUSED, STOPPED;
}

enum class PlaybackCompletion {
	WAITING, STARTED, FINISHED;
}
