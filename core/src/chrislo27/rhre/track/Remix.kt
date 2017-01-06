package chrislo27.rhre.track

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.HasGame
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.inspections.Inspections
import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.visual.VisualRegistry
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable
import ionium.registry.AssetRegistry

class Remix {

	val entities: MutableList<Entity> = mutableListOf()
	val selection: List<Entity> = mutableListOf()
	var tempoChanges: TempoChanges = TempoChanges(120f)
		private set

	private var playingState = PlayingState.STOPPED
	@Volatile
	var music: MusicData? = null
		set(value) {
			music?.dispose()

			field = value

			field?.music?.volume = musicVolume
		}
	@Volatile
	var musicVolume: Float = 1f
		set(value) {
			field = MathUtils.clamp(value, 0f, 1f)
			music?.music?.volume = field
		}

	private var beat: Float = 0f
	var musicStartTime: Float = 0f
	var playbackStart: Float = 0f
	private var endTime: Float = 0f
	private var startTime: Float = 0f
	var tickEachBeat = false
		set(value) {
			field = value
			lastTickBeat = beat.toInt()
		}
	private var lastTickBeat = Int.MIN_VALUE
	private var musicPlayed = false
	var currentGame: Game? = null
		private set

	val inspections: Inspections

	init {
		inspections = Inspections(this)
	}

	companion object {
		fun writeToObject(remix: Remix): RemixObject {
			with(remix) {
				val obj = RemixObject()

				obj.version = ionium.templates.Main.version

				obj.musicStartTime = musicStartTime
				obj.playbackStart = playbackStart
				obj.musicVolume = musicVolume

				obj.entities = arrayListOf()
				entities.forEach {
					val o: RemixObject.EntityObject = RemixObject.EntityObject()
					o.id = it.id
					o.beat = it.bounds.x
					o.level = it.bounds.y.toInt()
					o.width = it.bounds.width
					o.isPattern = it is PatternEntity
					o.semitone = it.semitone

					obj.entities.add(o)
				}

				obj.bpmChanges = arrayListOf()
				tempoChanges.getBeatMap().forEach {
					val o: RemixObject.BpmTrackerObject = RemixObject.BpmTrackerObject()

					o.beat = it.value.beat
					o.tempo = it.value.tempo

					obj.bpmChanges.add(o)
				}

				return obj
			}
		}

		fun readFromObject(obj: RemixObject): Remix {
			val remix: Remix = Remix()

			remix.playbackStart = obj.playbackStart
			remix.musicStartTime = obj.musicStartTime
			remix.musicVolume = obj.musicVolume

			remix.entities.clear()
			obj.entities?.forEach {
				val e: Entity

				if (it.isPattern) {
					e = PatternEntity(remix, GameRegistry.instance().getPatternRaw(it.id))

					e.bounds.x = it.beat
					e.bounds.y = it.level.toFloat()
					e.bounds.width = it.width
					e.onLengthChange(it.width)
					e.adjustPitch(it.semitone, -128, 128)
				} else {
					if (it.width == 0f) {
						e = SoundEntity(remix, GameRegistry.instance().getCueRaw(it.id), it.beat, it.level,
										it.semitone)
					} else {
						e = SoundEntity(remix, GameRegistry.instance().getCueRaw(it.id), it.beat, it.level, it.width,
										it.semitone)
					}
				}

				remix.entities.add(e)
			}

			remix.tempoChanges.clear()
			obj.bpmChanges?.forEach {
				val tc: TempoChange = TempoChange(it.beat, it.tempo, remix.tempoChanges)

				remix.tempoChanges.add(tc)
			}

			remix.inspections.refresh()

			return remix
		}
	}

	fun setPlayingState(ps: PlayingState): Unit {
		fun resetEntitiesAndTracker(): Unit {
			musicPlayed = false
			music?.music?.stop()
			// reset playback completion
			entities.forEach(Entity::reset)
			beat = playbackStart
			lastTickBeat = Int.MIN_VALUE
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
			currentGame = null
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
				updateDurationAndCurrentGame()
				AssetRegistry.instance().resumeAllSound()
			}
			PlayingState.PAUSED -> {
				music?.music?.pause()
				AssetRegistry.instance().pauseAllSound()
			}
			PlayingState.STOPPED -> {
				resetEntitiesAndTracker()
				AssetRegistry.instance().stopAllSound()
			}
		}
	}

	fun updateDurationAndCurrentGame() {
		endTime = entities.fold(Float.MIN_VALUE,
								{ value, entity -> Math.max(value, entity.bounds.x + entity.bounds.width) })
		startTime = entities.fold(Float.MAX_VALUE,
								  { value, entity -> Math.min(value, entity.bounds.x) })
	}

	fun getEndTime() = endTime
	fun getStartTime() = startTime
	fun getDuration() = endTime - startTime

	fun getPlayingState(): PlayingState = playingState

	fun getBeat(): Float = beat

	fun update(delta: Float): Unit {
		if (playingState != PlayingState.PLAYING)
			return@update

		if (music != null && (!music!!.music.isPlaying || !musicPlayed) && beat >= tempoChanges.secondsToBeats(
				musicStartTime)) {
			music!!.music.play()
			music!!.music.position = tempoChanges.beatsToSeconds(beat) - musicStartTime;
			musicPlayed = true
		}

		val lastBpm: Float = tempoChanges.getTempoAt(beat)

		// we rely on music being our timekeeper,
		// but delta time is required since music position only updates so frequently
		// if the internal beat is more than the music reported time, set it to the music time
		// otherwise increment by delta

		val musicInBeats: Float =
				if (music != null)
					tempoChanges.secondsToBeats(music!!.music.position + musicStartTime)
				else
					beat

		if (music != null && music!!.music.isPlaying && beat >= musicInBeats) {
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

		if (tickEachBeat && beat.toInt() > lastTickBeat) {
			lastTickBeat = beat.toInt()
			GameRegistry.instance()["countIn"].getCue("cowbell")?.getSoundObj()?.play(1f, 1.1f, 0f)
		}

		if (tempoChanges.getTempoAt(beat) != lastBpm) {
//			Gdx.app.postRunnable(AudioChangePitch(music!!.music))
		}

		val filtered = entities.filter { it.playbackCompletion == PlaybackCompletion.STARTED && it is HasGame }
		val anyDiffer = filtered.any {
			it as HasGame

			if (it.game.id == "countIn") return@any false

			return@any it.game != currentGame
		}

		if (anyDiffer) {
			val atLeastOne = filtered.any {
				it as HasGame
				return@any it.game == currentGame
			}

			if (!atLeastOne) {
				if (currentGame != null) {
					val renderer = VisualRegistry.map[currentGame!!.id]
					renderer?.onEnd(this)
				}
				currentGame = (entities
						.firstOrNull { it.playbackCompletion == PlaybackCompletion.STARTED && it is HasGame && it.game.id != "countIn" } as HasGame?)?.game
				if (currentGame != null) {
					val renderer = VisualRegistry.map[currentGame!!.id]
					renderer?.onStart(this)
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

data class MusicData(val music: Music, val file: FileHandle) : Disposable {

	override fun dispose() {
		music.dispose()
	}
}
