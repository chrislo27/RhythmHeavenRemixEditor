package io.github.chrislo27.rhre3.track

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.oopsies.ActionHistory
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.rhre2.RemixObject
import io.github.chrislo27.rhre3.tempo.TempoChange
import io.github.chrislo27.rhre3.tempo.Tempos
import io.github.chrislo27.rhre3.track.music.MusicData
import io.github.chrislo27.rhre3.track.music.MusicVolumeChange
import io.github.chrislo27.rhre3.track.music.MusicVolumes
import io.github.chrislo27.rhre3.track.timesignature.TimeSignatures
import io.github.chrislo27.rhre3.tracker.TrackerContainer
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.version.Version
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.properties.Delegates


class Remix(val camera: OrthographicCamera, val editor: Editor)
    : ActionHistory<Remix>(), Disposable {

    companion object {

        class RemixLoadInfo(val remix: Remix, val missing: Pair<Int, Int>,
                            val isAutosave: Boolean)

        /*

        To correctly do persistent data:

        * Update tracker blocks of code when new trackers are added
        * Update when statement in fromJson

         */

        fun toJson(remix: Remix, isAutosave: Boolean): ObjectNode {
            val tree = JsonHandler.OBJECT_MAPPER.createObjectNode()

            remix.apply {
                tree.put("version", RHRE3.VERSION.toString())
                tree.put("databaseVersion", GameRegistry.data.version)

                tree.put("playbackStart", playbackStart)
                tree.put("musicStartSec", musicStartSec)

                tree.put("isAutosave", isAutosave)

                // music
                run {
                    val obj = tree.putObject("musicData")
                    val music = music

                    obj.put("present", music != null)

                    if (music != null) {
                        obj.put("filename", music.handle.name())
                        obj.put("extension", music.handle.extension())
                    }
                }

                // entities
                val entitiesArray = tree.putArray("entities")
                entities.forEach { entity ->
                    val node = entitiesArray.addObject()

                    node.put("type", entity.jsonType)

                    entity.saveData(node)
                }

                // trackers
                run {
                    val trackers = tree.putObject("trackers")

                    trackers.set("tempos", tempos.toTree(trackers.objectNode()))
                    trackers.set("musicVolumes", musicVolumes.toTree(trackers.objectNode()))
                    trackers.set("timeSignatures", timeSignatures.toTree(trackers.objectNode()))
                }
            }

            return tree
        }

        fun fromJson(tree: ObjectNode, remix: Remix): RemixLoadInfo {
            remix.version = Version.fromString(tree["version"].asText())
            remix.databaseVersion = tree["databaseVersion"].asInt(-1)

            remix.playbackStart = tree["playbackStart"]?.floatValue() ?: 0f
            remix.musicStartSec = tree["musicStartSec"]?.floatValue() ?: 0f

            var missing = 0
            var missingCustom = 0

            // entities
            val entitiesArray = tree["entities"] as ArrayNode
            entitiesArray.filterIsInstance<ObjectNode>()
                    .filter { it.has("type") }
                    .forEach { node ->
                        val type = node["type"].asText(null) ?: return@forEach

                        val entity: Entity = when (type) {
                            "model" -> {
                                val datamodelID = node[ModelEntity.JSON_DATAMODEL].asText(null)
                                ?: run {
                                    missing++
                                    if (node["isCustom"].asBoolean(false))
                                        missingCustom++
                                    return@forEach
                                }
                                GameRegistry.data.objectMap[datamodelID]?.createEntity(remix, null)
                                        ?: return@forEach
                            }
                            else -> error("Unsupported entity type: $type")
                        }

                        entity.readData(node)

                        remix.entities += entity
                    }

            // trackers
            run {
                val trackers = tree.get("trackers") as ObjectNode

                remix.tempos.fromTree(trackers["tempos"] as ObjectNode)
                remix.musicVolumes.fromTree(trackers["musicVolumes"] as ObjectNode)
                remix.timeSignatures.fromTree(trackers["timeSignatures"] as ObjectNode)
            }

            return RemixLoadInfo(remix, missing to missingCustom,
                                 tree["isAutosave"]?.asBoolean(false) ?: false)
        }

        fun pack(remix: Remix, stream: ZipOutputStream, isAutosave: Boolean) {
            val objectNode = Remix.toJson(remix, isAutosave)
            stream.setComment("Rhythm Heaven Remix Editor 3 savefile - ${RHRE3.VERSION}")

            stream.putNextEntry(ZipEntry("remix.json"))
            stream.write(JsonHandler.toJson(objectNode).toByteArray())
            stream.closeEntry()

            val musicNode = objectNode["musicData"] as ObjectNode
            if (musicNode["present"].booleanValue()) {
                stream.putNextEntry(ZipEntry("music.bin"))
                val buf = remix.music!!.handle.read(2048)
                buf.copyTo(stream)
                buf.close()
                stream.closeEntry()
            }
        }

        fun unpack(remix: Remix, zip: ZipFile): RemixLoadInfo {
            val jsonStream = zip.getInputStream(zip.getEntry("remix.json"))
            val objectNode: ObjectNode = JsonHandler.OBJECT_MAPPER.readTree(jsonStream) as ObjectNode
            jsonStream.close()

            val musicNode = objectNode["musicData"] as ObjectNode
            val musicPresent = musicNode["present"].booleanValue()

            val result = Remix.fromJson(objectNode, remix)

            if (musicPresent) {
                val folder = RHRE3.tmpMusic
                val fh = folder.child(musicNode["filename"].asText(null) ?: error("Could not find music filename"))
                val musicStream = zip.getInputStream(zip.getEntry("music.bin"))
                fh.write(musicStream, false)
                musicStream.close()

                remix.music = MusicData(fh, remix)
            }

            return result
        }

        fun unpackRHRE2(remix: Remix, zip: ZipFile): RemixLoadInfo {
            val jsonStream = zip.getInputStream(zip.getEntry("data.json"))
            val remixObject: RemixObject = JsonHandler.fromJson(String(jsonStream.readBytes(), Charsets.UTF_8))
            jsonStream.close()

            val musicPresent = remixObject.musicAssociation != null
            var missing = 0

            remix.playbackStart = remixObject.playbackStart
            remix.musicStartSec = remixObject.musicStartTime
            remix.databaseVersion = 0
            remix.version = Version.fromString(remixObject.version ?: "v2.17.0")

            remixObject.bpmChanges?.forEach {
                remix.tempos.add(TempoChange(it.beat, it.tempo))
            }
            remix.musicVolumes.add(MusicVolumeChange(remix.tempos.secondsToBeats(remix.musicStartSec),
                                                     (remixObject.musicVolume * 100f).toInt().coerceIn(0, 100)))

            remixObject.entities?.forEach {
                val datamodel = GameRegistry.data.objectMap[it.id] ?: run {
                    missing++
                    return@forEach
                }

                val entity = datamodel.createEntity(remix, null)

                if (entity is IRepitchable) {
                    entity.semitone = it.semitone
                }

                entity.updateBounds {
                    entity.bounds.x = it.beat
                    entity.bounds.y = it.level.toFloat()
                    if (it.width > 0f) {
                        entity.bounds.width = it.width
                    }
                }

                remix.entities += entity
            }

            if (musicPresent) {
                val folder = RHRE3.tmpMusic
                val filename = remixObject.musicAssociation!!
                val fh = folder.child(filename)
                val musicStream = zip.getInputStream(zip.getEntry(filename))
                fh.write(musicStream, false)
                musicStream.close()

                remix.music = MusicData(fh, remix)
            }

            return RemixLoadInfo(remix, missing to missing, false)
        }

        fun saveTo(remix: Remix, file: File, isAutosave: Boolean) {
            if (!file.exists()) {
                file.createNewFile()
            }
            val stream = ZipOutputStream(FileOutputStream(file))
            pack(remix, stream, isAutosave)
            stream.close()
        }
    }

    val main: RHRE3Application
        get() = editor.main

    var version: Version = RHRE3.VERSION
        private set
    var databaseVersion: Int = -1
        private set

    val entities: MutableList<Entity> = mutableListOf()
    val trackers: MutableList<TrackerContainer<*>> = mutableListOf()
    val timeSignatures: TimeSignatures = run {
        val ts = TimeSignatures()
        trackers += ts
        ts
    }
    val musicVolumes: MusicVolumes = run {
        val mv = MusicVolumes()
        trackers += mv
        mv
    }
    val tempos: Tempos = run {
        val t = Tempos()
        trackers += t
        t
    }

    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(field)
        }
    var beat: Float = 0f
        private set

    var playbackStart: Float = 0f
    var musicStartSec: Float = 0f
    var music: MusicData? = null
        set(value) {
            field?.dispose()
            field = value
        }
    private var lastMusicPosition: Float = -1f
    var metronome: Boolean = false
        set(value) {
            field = value
            lastTickBeat = beat.toInt()
        }
    private var lastTickBeat = Int.MIN_VALUE
    private var scheduleMusicPlaying = true
    @Volatile
    var musicSeeking = false
    var duration: Float = Float.POSITIVE_INFINITY
        private set
    var lastPoint: Float = 0f
        private set
    val currentSubtitles: MutableList<SubtitleEntity> = mutableListOf()
    val currentSubtitlesReversed: Iterable<SubtitleEntity> = currentSubtitles.asReversed()
    var cuesMuted = false
    var currentGameGroup: Game? = null
        private set

    private val metronomeSFX: List<LazySound> by lazy {
        listOf(
                (GameRegistry.data.objectMap["countInEn/cowbell"] as? Cue)?.sound ?:
                        throw RuntimeException("Missing metronome sound")
              )
    }
    var isMusicMuted: Boolean by Delegates.observable(false) { prop, old, new ->
        setMusicVolume()
    }

    var playState: PlayState = PlayState.STOPPED
        set(value) {
            val old = field
            val music = music
            field = value
            when (field) {
                PlayState.STOPPED -> {
                    AssetRegistry.stopAllSounds()
                    music?.music?.pause()
                    GameRegistry.data.objectList.forEach {
                        if (it is Cue) {
                            it.stopAllSounds()
                        }
                    }
                    currentSubtitles.clear()
                    currentGameGroup = null
                }
                PlayState.PAUSED -> {
                    AssetRegistry.pauseAllSounds()
                    music?.music?.pause()
                    GameRegistry.data.objectList.forEach {
                        if (it is Cue) {
                            it.pauseAllSounds()
                        }
                    }
                }
                PlayState.PLAYING -> {
                    lastMusicPosition = -1f
                    scheduleMusicPlaying = true
                    AssetRegistry.resumeAllSounds()
                    if (old == PlayState.STOPPED) {
                        computeDuration()
                        seconds = tempos.beatsToSeconds(playbackStart)
                        entities.forEach {
                            if (it.getUpperUpdateableBound() < beat) {
                                it.playbackCompletion = PlaybackCompletion.FINISHED
                            } else {
                                it.playbackCompletion = PlaybackCompletion.WAITING
                            }
                        }

                        lastTickBeat = Math.ceil(playbackStart - 1.0).toInt()

                        if (editor.stage.tapalongStage.visible) {
                            editor.stage.tapalongStage.reset()
                        }

                        currentSubtitles.clear()
                    } else if (old == PlayState.PAUSED) {
                        GameRegistry.data.objectList.forEach {
                            if (it is Cue) {
                                it.resumeAllSounds()
                            }
                        }
                    }
                    if (music != null) {
                        if (seconds >= musicStartSec) {
                            music.music.play()
                            setMusicVolume()
                            if (old == PlayState.STOPPED) {
                                seekMusic()
                            }
                        } else {
                            music.music.stop()
                        }
                    }
                }
            }
        }

    private fun setMusicVolume() {
        val music = music ?: return
        val shouldBe = if (isMusicMuted) 0f else musicVolumes.getPercentageVolume(beat)
        if (music.music.volume != shouldBe) {
            music.music.volume = shouldBe
        }
    }

    private fun seekMusic() {
        val music = music ?: return
        musicSeeking = true
        music.music.position = seconds - musicStartSec
        musicSeeking = false
    }

    private fun computeDuration() {
        duration = entities.firstOrNull { it is EndEntity }?.bounds?.x ?: Float.POSITIVE_INFINITY
        lastPoint = getLastEntityPoint()
    }

    fun getLastEntityPoint(): Float {
        if (entities.isEmpty())
            return 0f
        return if (entities.isNotEmpty() && entities.any { it is EndEntity }) {
            entities.first { it is EndEntity }.bounds.x
        } else {
            val last = entities.maxBy { it.bounds.x + it.bounds.width }!!
            last.bounds.x + last.bounds.width
        }
    }

    fun entityUpdate(entity: Entity) {
        if (entity.playbackCompletion == PlaybackCompletion.WAITING) {
            if (entity.isUpdateable(beat)) {
                entity.playbackCompletion = PlaybackCompletion.PLAYING
                entity.onStart()

                if (entity is ModelEntity<*> && !entity.datamodel.game.noDisplay) {
                    currentGameGroup = entity.datamodel.game
                }
            }
        }

        if (entity.playbackCompletion == PlaybackCompletion.PLAYING) {
            entity.whilePlaying()

            if (entity.isFinished()) {
                entity.playbackCompletion = PlaybackCompletion.FINISHED
                entity.onEnd()
            }
        }
    }

    fun timeUpdate(delta: Float) {
        val music: MusicData? = music

        music?.music?.update(if (playState == PlayState.PLAYING) (delta * 0.75f) else delta)

        if (playState != PlayState.PLAYING)
            return

        seconds += delta
        if (music != null) {
            val currentSeconds = seconds
            if (scheduleMusicPlaying && seconds >= musicStartSec) {
                val ended = music.music.play()
                scheduleMusicPlaying = false
                if (ended) {
                    music.music.pause()
                }
            }
            if (music.music.isPlaying) {
                val oldPosition = lastMusicPosition
                val position = music.music.position
                lastMusicPosition = position

                if (oldPosition != position) {
                    seconds = position + musicStartSec
                }

                setMusicVolume()
            }
        }

        entities.forEach { entity ->
            if (entity.playbackCompletion != PlaybackCompletion.FINISHED) {
                entityUpdate(entity)
            }
        }

        if (Math.floor(beat.toDouble()) > lastTickBeat) {
            lastTickBeat = Math.floor(beat.toDouble()).toInt()
            if (metronome) {
                val isStartOfMeasure = timeSignatures.getMeasurePart(lastTickBeat.toFloat()) == 0
                metronomeSFX[Math.round(Math.abs(beat)) % metronomeSFX.size]
                        .sound.play(1f, if (isStartOfMeasure) 1.5f else 1.1f, 0f)
            }
        }

        if (playState != PlayState.STOPPED
                && (beat >= duration
                || main.preferences.getBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, false) && beat >= lastPoint)) {
            playState = PlayState.STOPPED
        }
    }

    override fun dispose() {
        music?.dispose()
    }
}