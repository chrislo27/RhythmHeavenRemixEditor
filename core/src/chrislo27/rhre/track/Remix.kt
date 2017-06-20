package chrislo27.rhre.track

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.HasGame
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.oopsies.ActionHistory
import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.script.luaobj.LuaRemix
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import ionium.registry.AssetRegistry
import ionium.registry.lazysound.LazySound
import ionium.templates.Main
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.sound.midi.*


class Remix : ActionHistory<Remix>() {

	val entities: MutableList<Entity> = mutableListOf()
	val selection: MutableList<Entity> = mutableListOf()
	var tempoChanges: TempoChanges = TempoChanges(120f)
		private set

	private val metronomeCowbell: LazySound by lazy {
		GameRegistry["countInEn"]!!.getCue("cowbell")?.getLazySoundObj() ?: throw RuntimeException("Missing cowbell sound")
	}

	@Volatile var playingState = PlayingState.STOPPED
		set(ps) {
			fun resetEntitiesAndTracker(): Unit {
				musicPlayed = PlaybackCompletion.WAITING
				music?.music?.stop()
//			music?.music?.position = tempoChanges.beatsToSeconds(playbackStart)
				// reset playback completion
				entities.forEach(Entity::reset)
				beat = playbackStart
				lastTickBeat = Math.ceil(playbackStart.toDouble() - 1).toInt()
				entities.forEach {
					if (it is PatternEntity) {
						it.internal.filter { inter -> it.bounds.x + inter.bounds.x < beat }.forEach { inter ->
							inter.playbackCompletion = PlaybackCompletion.FINISHED
							inter.onEnd(0f, it.bounds.x + inter.bounds.x + inter.bounds.width)
						}
					} else if (it.bounds.x + it.bounds.width < beat) {
						it.playbackCompletion = PlaybackCompletion.FINISHED
						it.onEnd(0f, it.bounds.x + it.bounds.width)
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

			field = ps

			// change to
			when (ps) {
				PlayingState.PLAYING -> {
					updateDurationAndCurrentGame()
					AssetRegistry.instance().resumeAllSound()
				}
				PlayingState.PAUSED -> {
					music?.music?.pause()
					musicPlayed = PlaybackCompletion.WAITING
					AssetRegistry.instance().pauseAllSound()
				}
				PlayingState.STOPPED -> {
					resetEntitiesAndTracker()
					AssetRegistry.instance().stopAllSound()
				}
			}
		}
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
			field = value.coerceIn(0.0f..1.0f)
			music?.music?.volume = field
		}

	var beat: Float = 0f
		private set
	var musicStartTime: Float = 0f
	var playbackStart: Float = 0f
	var endTime: Float = 0f
		private set
	var startTime: Float = 0f
		private set
	var duration: Float = endTime - startTime
		get() {
			return endTime - startTime
		}
		private set
	var tickEachBeat = false
		set(value) {
			field = value
			lastTickBeat = beat.toInt()
		}
	private var lastTickBeat = Int.MIN_VALUE
	private var musicPlayed: PlaybackCompletion = PlaybackCompletion.WAITING
	var currentGame: Game? = null
		private set

	var metadata: RemixObject.MetadataObject? = RemixObject.MetadataObject()

	@Volatile private var queueSweepLoad: Boolean = false

	@Volatile var sweepLoadProgress: Float = 0f
	@Volatile private var sweepLoadCount: Int = 0

	companion object {
		@JvmField
		var muteMusic: Boolean = false

		fun writeToJsonObject(remix: Remix): RemixObject {
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

					obj.entities!!.add(o)
				}

				obj.bpmChanges = arrayListOf()
				tempoChanges.getBeatMap().forEach {
					val o: RemixObject.BpmTrackerObject = RemixObject.BpmTrackerObject()

					o.beat = it.value.beat
					o.tempo = it.value.tempo

					obj.bpmChanges!!.add(o)
				}

				obj.metadata = metadata ?: RemixObject.MetadataObject()
				obj.metadata.gamesUsed = entities.filter { it is HasGame && it.game != GameRegistry["countIn"] }
						.map {
							it as HasGame
							return@map it.game.name
						}.distinct().sorted().joinToString(", ")

				return obj
			}
		}

		fun readFromJsonObject(obj: RemixObject): Remix {
			val remix: Remix = Remix()

			remix.playbackStart = obj.playbackStart
			remix.musicStartTime = obj.musicStartTime
			remix.musicVolume = obj.musicVolume

			remix.entities.clear()
			obj.entities?.forEach {
				val e: Entity

				if (it.isPattern) {
					e = PatternEntity(remix, GameRegistry.getPattern(it.id!!)!!)

					e.bounds.x = it.beat
					e.bounds.y = it.level.toFloat()
					e.bounds.width = it.width
					e.onLengthChange(it.width)
					e.adjustPitch(it.semitone, -128, 128)
				} else {
					if (it.width == 0f) {
						e = SoundEntity(remix, GameRegistry.getCue(it.id!!)!!, it.beat, it.level,
										it.semitone)
					} else {
						e = SoundEntity(remix, GameRegistry.getCue(it.id!!)!!, it.beat, it.level, it.width,
										it.semitone)
					}

					e.stopSoundAlways = it.stopAlways
				}

				e.volume = it.volume

				remix.entities.add(e)
			}

			remix.tempoChanges.clear()
			obj.bpmChanges?.forEach {
				val tc: TempoChange = TempoChange(it.beat, it.tempo, remix.tempoChanges)

				remix.tempoChanges.add(tc)
			}

			remix.metadata = obj.metadata

			remix.updateDurationAndCurrentGame()
			remix.queueSweepLoad = true

			if (obj.musicData != null)
				remix.music = obj.musicData

			return remix
		}

		fun writeToZipStream(remix: Remix, stream: ZipOutputStream) {
			stream.putNextEntry(ZipEntry("data.json"))
			val json = writeToJsonObject(remix)
			json.musicAssociation = remix.music?.file?.name()
			stream.write(JsonHandler.toJson(json).toByteArray(Charset.forName("UTF-8")))
			stream.closeEntry()

			if (json.musicAssociation != null) {
				stream.putNextEntry(ZipEntry(json.musicAssociation))

				val music = remix.music!!
				val reader = music.file.read(2048)
				val buf = ByteArray(2048)

				while (true) {
					val amt = reader.read(buf)

					if (amt <= 0)
						break

					stream.write(buf, 0, amt)
				}

				stream.closeEntry()
			}

			stream.finish()
			stream.close()
		}

		fun readFromZipStream(zipFile: ZipFile): RemixObject {
			val dataEntry: ZipEntry = zipFile.getEntry("data.json") ?: throw IllegalStateException(
					"data.json not found")
			val inputStream = zipFile.getInputStream(dataEntry)
			val result = ByteArrayOutputStream()
			val buffer = ByteArray(1024)
			var length: Int
			while (true) {
				length = inputStream.read(buffer)
				if (length <= 0)
					break

				result.write(buffer, 0, length)
			}
			inputStream.close()

			val obj = JsonHandler.fromJson(result.toString("UTF-8"), RemixObject::class.java)

			obj.musicData = null

			if (obj.musicAssociation != null) {
				val tmpFolder = Gdx.files.local("tmpMusic/").file()
				tmpFolder.mkdir()
				val tempFile: File = File(tmpFolder.absolutePath, obj.musicAssociation)
				tempFile.deleteOnExit()
				tmpFolder.deleteOnExit()

				val musicEntry: ZipEntry = zipFile.getEntry(obj.musicAssociation) ?: throw RuntimeException(
						"Music file not found!")
				val iS = zipFile.getInputStream(musicEntry)
				val baos = ByteArrayOutputStream()
				var newLength: Int
				while (true) {
					newLength = iS.read(buffer)
					if (newLength <= 0)
						break

					baos.write(buffer, 0, newLength)
				}
				iS.close()

				val out = FileOutputStream(tempFile)
				out.write(baos.toByteArray())
				out.close()

				val handle: FileHandle = FileHandle(tempFile)
				obj.musicData = MusicData(Gdx.audio.newMusic(handle), handle)
			}
			zipFile.close()
			return obj
		}

		fun readFromMidiSequence(sequence: Sequence): RemixObject {
			val beatsPerTick: Float = 1f / sequence.resolution

			val obj: RemixObject = RemixObject()
			obj.entities = mutableListOf()
			obj.bpmChanges = mutableListOf()
			obj.metadata = RemixObject.MetadataObject()
			obj.playbackStart = 0f

			data class NotePoint(val startBeat: Float, var duration: Float, val semitone: Int, val volume: Float, val track: Int)

			var trackNum: Int = 0
			val points: List<NotePoint> = sequence.tracks.flatMap { track ->
				var current: NotePoint? = null
				val list = mutableListOf<NotePoint>()

				for (i in 0 until track.size()) {
					val event: MidiEvent = track[i]
					val message: MidiMessage = event.message

					if (message is ShortMessage) {
						val command: Int = message.command
						val semitone: Int = message.data1 - 60

						fun endNote() {
							if (current != null) {
								current!!.duration = event.tick * beatsPerTick - current!!.startBeat
								list += current!!
								current = null
							}
						}

						when (command) {
							ShortMessage.NOTE_ON -> {
								endNote()
								current = NotePoint(event.tick * beatsPerTick, 0f, semitone, message.data2 / 127f, trackNum)
							}
							ShortMessage.NOTE_OFF -> {
								endNote()
							}
						}
					} else if (message is MetaMessage) {
						when (message.type) {
							0x51 /* SET_TEMPO */ -> {
								val data = message.data
								val microseconds: Int = ((data[0].toInt() and 0xFF) shl 16) or ((data[1].toInt() and 0xFF) shl 8) or (data[2].toInt() and 0xFF)
								val bpm: Float = 60_000_000f / microseconds

								val bpmTrackerObject = RemixObject.BpmTrackerObject()
								bpmTrackerObject.beat = event.tick * beatsPerTick
								bpmTrackerObject.tempo = bpm
								obj.bpmChanges!! += bpmTrackerObject
							}
						}
					}
				}

				trackNum++
				return@flatMap list
			}

			val noteCue: String = "builtToScaleDS/c" // TODO

			val entities: MutableList<RemixObject.EntityObject> = obj.entities!!

			points.forEach { point ->
				val ent = RemixObject.EntityObject()

				ent.id = noteCue
//				ent.id = "quizShow/contestantA"
				if (point.track == 9) {
//					ent.id = "bossaNovaEn/ball"
				}

				ent.beat = point.startBeat
				ent.width = point.duration
				ent.semitone = point.semitone
				ent.level = point.track % Editor.TRACK_COUNT
				ent.isPattern = !ent.id!!.contains('/')
//				ent.stopAlways = true

				entities.add(ent)
			}

			return obj
		}
	}

	fun getLuaValue(globals: Globals): LuaValue = CoerceJavaToLua.coerce(LuaRemix(globals, this))

	fun updateDurationAndCurrentGame() {
		endTime = entities.fold(Float.MIN_VALUE,
								{ value, entity -> Math.max(value, entity.bounds.x + entity.bounds.width) })
		startTime = entities.fold(Float.MAX_VALUE,
								  { value, entity -> Math.min(value, entity.bounds.x) })
	}

	fun getBeatBounce(): Float {
		val beatDec = beat - beat.toInt()

		if (beatDec <= 0.3f && beat >= 0 && beat < duration) {
			return 1f - beatDec / 0.3f
		} else {
			return 0f
		}
	}

	fun update(delta: Float): Unit {
		if (playingState != PlayingState.PLAYING) {
			if (queueSweepLoad) {
				queueSweepLoad = false
				sweepLoadProgress = 0f
				sweepLoadCount = 0
				val cachedEntities = entities.flatMap {
					if (it is PatternEntity) {
						return@flatMap it.internal
					}
					return@flatMap listOf(it)
				}.distinctBy { it.id }.filter(Entity::needsToBeLoaded)
				val coroutines = mutableListOf<Job>()
				val idsPerCoroutine: Int = 64 // 145, 74, 49

				val nano = System.nanoTime()
				(0..cachedEntities.size - 1 step idsPerCoroutine).mapTo(coroutines) {
					launch(CommonPool) {
						for (index in it..Math.min(it + idsPerCoroutine, cachedEntities.size - 1)) {
							cachedEntities[index].attemptLoadSounds()
							sweepLoadCount++
							sweepLoadProgress = sweepLoadCount.toFloat() / cachedEntities.size
						}
					}
				}

				Main.logger.info(
						"Starting sweep-load with ${cachedEntities.size} IDs and ${coroutines.size} coroutines")

				launch(CommonPool) {
					for (c in coroutines) {
						c.join()
					}

					sweepLoadProgress = 1f
					Main.logger.info(
							"Finished sweep-load in ${(System.nanoTime() - nano) / 1_000_000.0} ms")
				}
			}

			return@update
		}

		if (music != null && (musicPlayed == PlaybackCompletion.WAITING && !music!!.music.isPlaying) &&
				(beat >= tempoChanges.secondsToBeats(musicStartTime))) {
			val newPosition = tempoChanges.beatsToSeconds(beat) - musicStartTime

			music!!.music.play()
			music!!.music.position = newPosition
			musicPlayed = PlaybackCompletion.STARTED
			music!!.music.setOnCompletionListener {
				musicPlayed = PlaybackCompletion.FINISHED
			}
			beat = tempoChanges.secondsToBeats(music!!.music.position + musicStartTime)

			return
		}

		music?.music?.volume = if (muteMusic) 0f else musicVolume

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

		beat = tempoChanges.secondsToBeats(tempoChanges.beatsToSeconds(beat) + delta)

		if (music != null && music!!.music.isPlaying && beat >= musicInBeats) {
			beat = musicInBeats
		}

		for (e: Entity in entities) {
			if (e.playbackCompletion == PlaybackCompletion.FINISHED) continue

			if (beat >= e.bounds.x) {
				if (e.playbackCompletion == PlaybackCompletion.WAITING) {
					e.onStart(delta, e.bounds.x)
					e.playbackCompletion = PlaybackCompletion.STARTED
				}

				if (e.playbackCompletion == PlaybackCompletion.STARTED) {
					e.onWhile(delta)

					if (beat >= e.bounds.x + e.bounds.width) {
						e.onEnd(delta, e.bounds.x + e.bounds.width)
						e.playbackCompletion = PlaybackCompletion.FINISHED
					}
				}
			}
		}

		if (tickEachBeat && beat.toInt() > lastTickBeat && metronomeCowbell.isLoaded) {
			lastTickBeat = beat.toInt()
			metronomeCowbell.sound.play(1f, 1.1f, 0f)
		}

		if (tempoChanges.getTempoAt(beat) != lastBpm && music?.music != null) {
//			MusicUtils.changePitch(music!!.music as OpenALMusic, tempoChanges.getTempoAt(beat) / 120)
		}

		val filtered = entities.filter { it.playbackCompletion == PlaybackCompletion.STARTED && it is HasGame }
		val anyDiffer = filtered.any {
			it as HasGame

			if (it.game.notRealGame) return@any false

			return@any it.game != currentGame
		}

		if (anyDiffer) {
			val atLeastOne = filtered.any {
				it as HasGame
				return@any it.game == currentGame
			}

			if (!atLeastOne) {
				currentGame = (entities
						.firstOrNull { it.playbackCompletion == PlaybackCompletion.STARTED && it is HasGame && !it.game.notRealGame } as HasGame?)?.game
			}
		}

		if (playingState == PlayingState.PLAYING && beat >= endTime)
			playingState = PlayingState.STOPPED
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
