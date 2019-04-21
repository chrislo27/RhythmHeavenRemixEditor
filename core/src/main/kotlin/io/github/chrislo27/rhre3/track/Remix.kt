package io.github.chrislo27.rhre3.track

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.VersionHistory
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.entity.model.special.*
import io.github.chrislo27.rhre3.oopsies.ActionHistory
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.rhre2.RemixObject
import io.github.chrislo27.rhre3.soundsystem.LazySound
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsMusic
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.track.timesignature.TimeSignature
import io.github.chrislo27.rhre3.track.timesignature.TimeSignatures
import io.github.chrislo27.rhre3.track.tracker.TrackerContainer
import io.github.chrislo27.rhre3.track.tracker.musicvolume.MusicVolumeChange
import io.github.chrislo27.rhre3.track.tracker.musicvolume.MusicVolumes
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChanges
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.rhre3.util.Swing
import io.github.chrislo27.rhre3.util.settableLazy
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import io.github.chrislo27.toolboks.version.Version
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.sound.midi.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates


open class Remix(val main: RHRE3Application)
    : ActionHistory<Remix>(), Disposable {

    companion object {

        val DEFAULT_MIDI_NOTE: String = "gleeClubEn/singLoop"

        class RemixLoadInfo(val remix: Remix, val missing: Pair<Int, Int>,
                            val isAutosave: Boolean,
                            val extra: MutableMap<String, Any> = mutableMapOf())

        fun createMissingEntitySubtitle(remix: Remix, id: String, x: Float, y: Float, w: Float, h: Float): SubtitleEntity {
            return (GameRegistry.data.objectMap["special_subtitleEntity"] as Subtitle).createEntity(remix, null).apply {
                this.subtitle = "[RED]MISSING ENTITY[]\n$id"
                this.updateBounds {
                    this.bounds.set(x, y, w, h)
                }
            }
        }

        fun toJson(remix: Remix, isAutosave: Boolean): ObjectNode {
            val tree = JsonHandler.OBJECT_MAPPER.createObjectNode()

            remix.apply {
                tree.put("version", RHRE3.VERSION.toString())
                tree.put("databaseVersion", GameRegistry.data.version)

                tree.put("playbackStart", playbackStart)
                tree.put("musicStartSec", musicStartSec)

                tree.put("trackCount", trackCount)

                tree.put("isAutosave", isAutosave)
                tree.put("midiInstruments", midiInstruments)

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

                tree.putArray("textures").also { ar ->
                    textureCache.forEach { ar.add(it.key) }
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
                }

                // time signatures
                run {
                    val timeSigs = tree.putArray("timeSignatures")

                    timeSignatures.map.values.forEach {
                        val node = timeSigs.addObject()
                        node.put("beat", it.beat)
                        node.put("divisions", it.beatsPerMeasure)
                        node.put("beatUnit", it.beatUnit)
                        node.put("measure", it.measure)
                    }
                }
            }

            return tree
        }

        fun fromJson(tree: ObjectNode, remix: Remix): RemixLoadInfo {
            remix.version = Version.fromString(tree["version"].asText())
            remix.databaseVersion = tree["databaseVersion"].asInt(-1)

            remix.playbackStart = tree["playbackStart"]?.floatValue() ?: 0f
            remix.musicStartSec = tree["musicStartSec"]?.floatValue() ?: 0f

            remix.midiInstruments = tree["midiInstruments"]?.intValue() ?: 0

            remix.trackCount = tree["trackCount"]?.intValue()?.coerceAtLeast(1) ?: Editor.DEFAULT_TRACK_COUNT

            var missing = 0
            var missingCustom = 0

            fun fromThisVersion(version: Version): Boolean {
                return remix.version < version && RHRE3.VERSION >= version
            }

            // backwards compatibility silent upgrades
            val shouldConvertTimeSignatures = fromThisVersion(VersionHistory.TIME_SIGNATURES_REFACTOR)

            // entities
            val entitiesArray = tree["entities"] as ArrayNode
            entitiesArray.filterIsInstance<ObjectNode>()
                    .filter { it.has("type") }
                    .forEach { node ->
                        val type = node["type"]?.asText(null) ?: return@forEach
                        val isCustom = node["isCustom"]?.asBoolean(false) ?: false

                        val entity: Entity = Entity.getEntityFromType(type, node, remix) ?: run {
                            missing++
                            if (isCustom)
                                missingCustom++

                            Toolboks.LOGGER.warn("Missing ${if (isCustom) "custom " else ""}asset: ${node[ModelEntity.JSON_DATAMODEL].asText(null)}")
                            remix.entities += createMissingEntitySubtitle(remix, node[ModelEntity.JSON_DATAMODEL]?.textValue() ?: "null", node["beat"]!!.floatValue(), node["track"]!!.floatValue(), node["width"]!!.floatValue(), node["height"]!!.floatValue())
                            return@forEach
                        }

                        entity.readData(node)

                        remix.entities += entity
                    }

            // trackers
            run {
                val trackers = tree.get("trackers") as ObjectNode

                remix.tempos.fromTree(trackers["tempos"] as ObjectNode)
                remix.musicVolumes.fromTree(trackers["musicVolumes"] as ObjectNode)
            }

            // time signatures
            run {
                if (shouldConvertTimeSignatures) {
                    val trackers = tree.get("trackers") as ObjectNode
                    val timeSigs = trackers["timeSignatures"] as ObjectNode
                    (timeSigs["trackers"] as ArrayNode).filterIsInstance<ObjectNode>().forEach {
                        remix.timeSignatures.add(TimeSignature(remix.timeSignatures, it["beat"].asDouble().toFloat(),
                                                               it["upper"].asInt(4), 4))
                    }
                } else {
                    val timeSigs = tree.get("timeSignatures") as ArrayNode
                    timeSigs.filterIsInstance<ObjectNode>().forEach {
                        remix.timeSignatures.add(
                                TimeSignature(remix.timeSignatures, it["beat"].asDouble().toFloat(), it["divisions"].asInt(4), it["beatUnit"]?.asInt(4) ?: 4))
                    }
                }
            }

            return RemixLoadInfo(remix, missing to missingCustom,
                                 tree["isAutosave"]?.asBoolean(false) ?: false)
        }

        fun fromMidiSequence(remix: Remix, sequence: Sequence): RemixLoadInfo {
            data class NotePoint(val startBeat: Float, var duration: Float, val semitone: Int, val volume: Float,
                                 val track: Pair<Track, Int>)

            val beatsPerTick: Float = 1f / sequence.resolution

            val tracksWithNotes: MutableList<Int> = mutableListOf()

            remix.tempos.clear()

            val points: List<NotePoint> = sequence.tracks.flatMap { track ->
                val list = mutableListOf<NotePoint>()
                val map = mutableMapOf<Int, NotePoint>()
                val trackIndex = sequence.tracks.indexOf(track)
                val pair = track to trackIndex

                for (i in 0 until track.size()) {
                    val event: MidiEvent = track[i]
                    val message: MidiMessage = event.message

                    if (message is ShortMessage) {
                        val command: Int = message.command
                        val semitone: Int = message.data1 - 60

                        fun turnOff() {
                            val point = map[semitone]
                            if (point != null) {
                                point.duration = event.tick * beatsPerTick - point.startBeat
                                list += point
                                map.remove(semitone)

                                if (trackIndex !in tracksWithNotes) {
                                    tracksWithNotes += trackIndex
                                }
                            }
                        }

                        when (command) {
                            ShortMessage.NOTE_ON -> {
                                val vol = message.data2 / 127f
                                if (vol <= 0) {
                                    turnOff()
                                } else {
                                    map[semitone] = NotePoint(event.tick * beatsPerTick, 0f, semitone, vol,
                                                              pair)
                                }
                            }
                            ShortMessage.NOTE_OFF -> {
                                turnOff()
                            }
                            else -> {
//                                println("Got command $command ${message.data1} ${message.data2}")
                            }
                        }
                    } else if (message is MetaMessage) {
                        // http://www.deluge.co/?q=midi-tempo-bpm
                        when (message.type) {
                            0x51 /* SET_TEMPO */ -> {
                                val data = message.data
                                val microseconds: Int = ((data[0].toInt() and 0xFF) shl 16) or ((data[1].toInt() and 0xFF) shl 8) or (data[2].toInt() and 0xFF)
                                val bpm: Float = 60_000_000f / microseconds

                                remix.tempos.add(TempoChange(remix.tempos, event.tick * beatsPerTick, bpm, Swing.STRAIGHT, 0f))
                            }
                            0x58 /* TIME_SIGNATURE */ -> {
                                val data = message.data
                                val numerator = data[0].toInt() and 0xFF
                                val denominatorPower = data[1].toInt() and 0xFF
                                val denominator = Math.pow(2.0, denominatorPower.toDouble()).toInt()

                                if (denominator >= 2) {
                                    remix.timeSignatures.add(
                                            TimeSignature(remix.timeSignatures,
                                                          (event.tick * beatsPerTick), numerator, denominator))
                                }
                            }
                        }
                    }
                }

                list
            }

            remix.midiInstruments = tracksWithNotes.size
            remix.trackCount = remix.midiInstruments.coerceIn(Editor.DEFAULT_TRACK_COUNT, Editor.MAX_TRACK_COUNT)

            val defaultCue = GameRegistry.data.objectMap[DEFAULT_MIDI_NOTE]!! as Cue
            val noteCue = GameRegistry.data.objectMap[remix.main.preferences.getString(
                    PreferenceKeys.MIDI_NOTE)] ?: defaultCue
            points.mapTo(remix.entities) { point ->
                val ent = noteCue.createEntity(remix, null).apply {
                    if (this is CueEntity) {
                        instrument = tracksWithNotes.indexOf(point.track.second) + 1
                    }

                    updateBounds {
                        bounds.set(point.startBeat,
                                   tracksWithNotes.indexOf(point.track.second).toFloat() % remix.trackCount,
                                   point.duration, 1f)
                    }

                    if (this is IRepitchable) {
                        semitone = point.semitone

                        if (this is CueEntity && semitone < -18) {
                            stopAtEnd = true
                        }
                    }
                }

                ent
            }

            // add end entity either 2 beats after furthest point, or on the next measure border
            remix.entities += GameRegistry.data.endRemix.createEntity(remix, null).apply {
                updateBounds {
                    val furthest = (remix.entities.maxBy { it.bounds.maxX }?.run { bounds.maxX }?.roundToInt()
                            ?: 0).toFloat()
                    val timeSig = remix.timeSignatures.getTimeSignature(furthest)
                    bounds.x = if (timeSig == null) {
                        furthest + 2f
                    } else {
                        (remix.timeSignatures.getMeasure(
                                furthest) + 1f - timeSig.measure) * timeSig.beatsPerMeasure + timeSig.beat
                    }
                    bounds.y = 0f
                }
            }

            // remove redundant tempo changes
            remix.tempos.map.values.toList()
                    .sortedBy(TempoChange::beat)
                    .fold(null as TempoChange?) { last, tc ->
                        if (last != null) {
                            if (tc.isZeroWidth && last.bpm == tc.bpm) {
                                remix.tempos.remove(tc)
                            }
                        }

                        tc
                    }

            // remove redundant time signatures
            remix.timeSignatures.map.values.toList()
                    .sortedBy(TimeSignature::beat)
                    .fold(null as TimeSignature?) { last, ts ->
                        if (last != null) {
                            if (ts.beatsPerMeasure == last.beatsPerMeasure) {
                                remix.timeSignatures.remove(ts)
                            }
                        }

                        ts
                    }

            return RemixLoadInfo(remix, 0 to 0, false).apply {
                extra["noteCue"] = noteCue
            }
        }

        fun pack(remix: Remix, stream: ZipOutputStream, isAutosave: Boolean) {
            val objectNode = Remix.toJson(remix, isAutosave)
            stream.setComment("Rhythm Heaven Remix Editor 3 savefile - ${RHRE3.VERSION}")

            stream.putNextEntry(ZipEntry("remix.json"))
            JsonHandler.toJson(objectNode, stream)
            stream.closeEntry()

            val musicNode = objectNode["musicData"] as ObjectNode
            if (musicNode["present"].booleanValue()) {
                stream.putNextEntry(ZipEntry("music.bin"))
                val buf = remix.music!!.handle.read(2048)
                buf.copyTo(stream)
                buf.close()
                stream.closeEntry()
            }

            if (remix.textureCache.isNotEmpty()) {
                remix.textureCache.forEach { name, tex ->
                    stream.putNextEntry(ZipEntry("$name.png"))

                    writeTexture(stream, tex)

                    stream.closeEntry()
                }
            }
        }

        /**
         * Must NOT be called in the GL context thread! Will deadlock if this happens.
         */
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

            val texs = objectNode["textures"] as? ArrayNode?
            if (texs != null) {
                val toLoad = texs.size()
                val loaded: MutableMap<String, Pair<Texture?, Throwable?>> = mutableMapOf()
                texs.forEach { node ->
                    Gdx.app.postRunnable {
                        val name = node.asText()
                        try {
                            val entry = zip.getEntry("$name.png")
                            val bytes = zip.getInputStream(entry).let { stream ->
                                val b = stream.readBytes()
                                stream.close()
                                b
                            }
                            val texture = Texture(Pixmap(bytes, 0, bytes.size))
                            loaded[name] = texture to null
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            loaded[name] = null to t
                        }
                    }
                }

                while (loaded.size < toLoad) {
                    Thread.sleep(10L)
                }

                if (loaded.any { it.value.second != null }) {
                    throw loaded.values.first { it.second != null }.second!!
                } else {
                    loaded.forEach { key, pair ->
                        remix.textureCache[key] = pair.first!!
                    }
                }
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

            remix.tempos.clear()
            remixObject.bpmChanges?.forEach {
                remix.tempos.add(TempoChange(remix.tempos, it.beat, it.tempo, Swing.STRAIGHT, 0f))
            }
            remix.musicVolumes.add(
                    MusicVolumeChange(remix.musicVolumes,
                                      remix.tempos.secondsToBeats(remix.musicStartSec),
                                      0f,
                                      (remixObject.musicVolume * 100f).toInt().coerceIn(0, MusicVolumeChange.MAX_VOLUME)
                                     ))

            remixObject.entities?.forEach {
                val datamodel = GameRegistry.data.objectMap[it.id] ?: run {
                    missing++
                    remix.entities += createMissingEntitySubtitle(remix, it.id ?: "null", it.beat, it.level.toFloat(), it.width, 1f)
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

                    if (entity is EquidistantEntity) {
                        entity.bounds.width /= entity.datamodel.cues.count { it.track == 0 }
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

        fun writeTexture(stream: OutputStream, tex: Texture) {
            if (!tex.textureData.isPrepared) {
                tex.textureData.prepare()
            }
            val pixmap: Pixmap = tex.textureData.consumePixmap()

            try {
                val writer = PixmapIO.PNG((pixmap.width.toFloat() * pixmap.height.toFloat() * 1.5f).toInt()) // Guess at deflated size.
                try {
                    writer.setFlipY(false)
                    writer.write(stream, pixmap)
                } finally {
                    writer.dispose()
                }
            } catch (ex: IOException) {
                throw GdxRuntimeException("Error writing PNG", ex)
            } finally {
                if (tex.textureData.disposePixmap()) {
                    pixmap.dispose()
                }
            }
        }
    }

    enum class EntityUpdateResult {
        NOT_STARTED, STARTED, UPDATED, ENDED, STARTED_AND_ENDED, ALREADY_UPDATED
    }

    var version: Version = RHRE3.VERSION
        private set
    var databaseVersion: Int = -1
        private set

    val entities: MutableList<Entity> = mutableListOf()
    val timeSignatures: TimeSignatures = TimeSignatures()
    val trackers: MutableList<TrackerContainer<*>> = mutableListOf()
    val trackersReverseView: List<TrackerContainer<*>> = trackers.asReversed()
    val tempos: TempoChanges = TempoChanges().apply { trackers.add(this) }
    val musicVolumes: MusicVolumes = MusicVolumes().apply { trackers.add(this) }

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
            lastMetronomeMeasure = -1
            lastMetronomeMeasurePart = -1
        }
    open var cuesMuted = false
    private var lastMetronomeMeasure: Int = -1
    private var lastMetronomeMeasurePart: Int = -1
    private var scheduleMusicPlaying = true
    @Volatile
    var musicSeeking = false
    /**
     * Only used for loading midi files
     */
    var midiInstruments = 0
        private set
    var trackCount: Int by Delegates.vetoable(Editor.DEFAULT_TRACK_COUNT) { _, _, new ->
        val allowed = new >= 1
        if (allowed) {
            entities.filterIsInstance<EndRemixEntity>().forEach { it.onTrackSizeChange(new) }
        }
        allowed
    }

    var speedMultiplier: Float = 1f

    var duration: Float = Float.POSITIVE_INFINITY
        private set
    var lastPoint: Float = 0f
        private set
    var entitiesTouchTrackTop: Boolean = false
        private set

    val currentSubtitles: MutableList<SubtitleEntity> = mutableListOf()
    val currentSubtitlesReversed: Iterable<SubtitleEntity> = currentSubtitles.asReversed()
    val currentShakeEntities: MutableList<ShakeEntity> = mutableListOf()

    val gameSections: NavigableMap<Float, GameSection> = TreeMap()
    var playalong: Playalong by settableLazy { createPlayalongInstance() }
    open var doUpdatePlayalong: Boolean = false

    private val metronomeSFX: LazySound by lazy {
                (GameRegistry.data.objectMap["countInEn/cowbell"] as? Cue)?.sound ?: error("Missing metronome sound")
    }
    var isMusicMuted: Boolean by Delegates.observable(false) { _, _, _ ->
        setMusicVolume()
    }

    val textureCache: MutableMap<String, Texture> = mutableMapOf()

    val playStateListeners: MutableList<(old: PlayState, new: PlayState) -> Unit> = mutableListOf()
    var playState: PlayState by Delegates.vetoable(PlayState.STOPPED) { _, old, new ->
        val music = music
        playStateListeners.forEach { it.invoke(old, new) }
        when (new) {
            PlayState.STOPPED -> {
                AssetRegistry.stopAllSounds()
                music?.music?.pause()
                BeadsSoundSystem.stop()
                currentSubtitles.clear()
                currentShakeEntities.clear()
            }
            PlayState.PAUSED -> {
                AssetRegistry.pauseAllSounds()
                music?.music?.pause()
                BeadsSoundSystem.pause()
            }
            PlayState.PLAYING -> {
                lastMusicPosition = -1f
                scheduleMusicPlaying = true
                AssetRegistry.resumeAllSounds()
                if (old == PlayState.STOPPED) {
                    recomputeCachedData()
                    seconds = tempos.beatsToSeconds(playbackStart)
                    entities.forEach {
                        if (it.getUpperUpdateableBound() < beat) {
                            it.playbackCompletion = PlaybackCompletion.FINISHED
                        } else {
                            it.playbackCompletion = PlaybackCompletion.WAITING
                        }
                    }

                    lastMetronomeMeasure = Math.ceil(playbackStart - 1.0).toInt()
                    lastMetronomeMeasurePart = -1

                    currentSubtitles.clear()
                    currentShakeEntities.clear()
                }
                BeadsSoundSystem.resume()
                if (music != null) {
                    if (seconds >= musicStartSec) {
                        music.music.play()
                        setMusicVolume()
                        seekMusic()
                    } else {
                        music.music.stop()
                    }
                }
            }
        }

        true
    }

    private fun setMusicVolume() {
        val music = music ?: return
        val shouldBe = if (isMusicMuted) 0f else musicVolumes.volumeAt(beat)
        if (music.music.getVolume() != shouldBe) {
            music.music.setVolume(shouldBe)
        }
    }

    private fun seekMusic() {
        val music = music ?: return
        musicSeeking = true
        music.music.setPosition(seconds - musicStartSec)
        musicSeeking = false
    }

    fun wouldEntitiesFitNewTrackCount(newCount: Int): Boolean {
        if (newCount < 1) {
            return false
        }

        return entities.filterNot { it is EndRemixEntity }.firstOrNull { (it.bounds.y + it.bounds.height).roundToInt() >= trackCount } == null
    }

    fun canIncreaseTrackCount(): Boolean = trackCount < Editor.MAX_TRACK_COUNT
    fun canDecreaseTrackCount(): Boolean = trackCount > Editor.MIN_TRACK_COUNT

    fun isEmpty(): Boolean {
        return entities.isEmpty() && trackers.all { it.map.isEmpty() } && timeSignatures.map.isEmpty() && music == null
    }

    /**
     * Call whenever entities move.
     */
    fun recomputeCachedData() {
        entities.sortBy { it.bounds.x }
        duration = entities.firstOrNull { it is EndRemixEntity }?.bounds?.x ?: Float.POSITIVE_INFINITY
        lastPoint = getLastEntityPoint()
        entitiesTouchTrackTop = entities.filterNot { it is EndRemixEntity }.firstOrNull { (it.bounds.y + it.bounds.height).toInt() >= trackCount } != null

        gameSections.clear()
        val reversedEntities = entities.takeWhile { it !is EndRemixEntity }
                .filterIsInstance<ModelEntity<*>>()
                .filter { !it.datamodel.game.noDisplay }
                .asReversed()
        var currentGame: Game? = null
        var currentEndPoint: Float = lastPoint
        reversedEntities.forEachIndexed { index, modelEntity ->
            val entityGame = modelEntity.datamodel.game
            val cachedCurrent = currentGame
            if (entityGame != currentGame) {
                currentGame = entityGame

                if (cachedCurrent != null) {
                    val previous = reversedEntities[index - 1]
                    val section = GameSection(previous.bounds.x, currentEndPoint, cachedCurrent)

                    gameSections[section.startBeat] = section

                    currentEndPoint = previous.bounds.x
                }
            }
        }
        if (currentGame != null) {
            val final = reversedEntities.last()
            val section = GameSection(final.bounds.x, currentEndPoint, final.datamodel.game)

            gameSections[section.startBeat] = section
        }

        val textureEntities = entities.filterIsInstance<TextureEntity>()
        if (textureEntities.isEmpty()) {
            if (textureCache.isNotEmpty()) {
                textureCache.values.toList().forEach(Texture::dispose)
                textureCache.clear()
            }
        } else {
            textureCache.keys.toList().filter { key ->
                textureEntities.none { it.textureHash == key }
            }.forEach { key ->
                textureCache.remove(key)
            }
        }

        if (playState == PlayState.STOPPED) {
            playalong = createPlayalongInstance()
        }
    }

    protected open fun createPlayalongInstance(): Playalong = Playalong(this)

    fun getLastEntityPoint(): Float {
        if (entities.isEmpty())
            return 0f
        return if (entities.isNotEmpty() && entities.any { it is EndRemixEntity }) {
            entities.first { it is EndRemixEntity }.bounds.x
        } else {
            val last = entities.maxBy { it.bounds.x + it.bounds.width }!!
            last.bounds.x + last.bounds.width
        }
    }

    fun getGameSection(beat: Float): GameSection? {
        val entry = gameSections.lowerEntry(beat) ?: return null
        return entry.value
    }

    fun entityUpdate(entity: Entity): EntityUpdateResult {
        if (entity.playbackCompletion == PlaybackCompletion.FINISHED) {
            return EntityUpdateResult.ALREADY_UPDATED
        }

        val started: Boolean

        if (entity.playbackCompletion == PlaybackCompletion.WAITING) {
            if (entity.isUpdateable(beat)) {
                entity.playbackCompletion = PlaybackCompletion.PLAYING
                entity.onStart()
                started = true
            } else {
                return EntityUpdateResult.NOT_STARTED
            }
        } else {
            started = false
        }

        if (entity.playbackCompletion == PlaybackCompletion.PLAYING) {
            entity.whilePlaying()

            if (entity.isFinished()) {
                entity.playbackCompletion = PlaybackCompletion.FINISHED
                entity.onEnd()

                return if (started) EntityUpdateResult.STARTED_AND_ENDED else EntityUpdateResult.ENDED
            }

            return if (started) EntityUpdateResult.STARTED else EntityUpdateResult.UPDATED
        }

        throw ConcurrentModificationException(
                "Entity playbackCompletion state was modified in such a way that it escaped all update logic branches")
    }

    fun timeUpdate(delta: Float) {
        val music: MusicData? = music

        music?.music?.update(speedMultiplier * if (playState == PlayState.PLAYING) (delta * 0.75f) else delta)

        if (playState != PlayState.PLAYING)
            return

        seconds += delta * speedMultiplier
        if (music != null) {
            if (scheduleMusicPlaying && seconds >= musicStartSec) {
                music.music.play()
                music.music.setPitch(speedMultiplier)
                scheduleMusicPlaying = false
//                if (ended) {
//                    music.music.pause()
//                }
            }
            if (music.music.isPlaying()) {
                val oldPosition = lastMusicPosition
                val position = music.music.getPosition()
                lastMusicPosition = position

                if (oldPosition != position) {
                    seconds = position + musicStartSec
                }

                setMusicVolume()
                music.music.setPitch(speedMultiplier)

                // Music distort
                val beadsMusic: BeadsMusic? = music.music as? BeadsMusic
                if (beadsMusic != null) {
                    val inDistortion = entities.any { it is MusicDistortEntity && beat in it.bounds.x..it.bounds.maxX }
                    val currentlyDistorted = beadsMusic.player.doBandpass
                    if (inDistortion != currentlyDistorted) {
                        beadsMusic.player.doBandpass = inDistortion
                    }
                }
            }
        }

        entities.forEach { entity ->
            if (entity.playbackCompletion != PlaybackCompletion.FINISHED) {
                entityUpdate(entity)
            }
        }

        if (doUpdatePlayalong) {
            playalong.frameUpdate()
        }

        val measure = timeSignatures.getMeasure(beat).takeIf { it >= 0 } ?: Math.floor(beat.toDouble()).toInt()
        val measurePart = timeSignatures.getMeasurePart(beat)
        if (lastMetronomeMeasure != measure || lastMetronomeMeasurePart != measurePart) {
            lastMetronomeMeasure = measure
            lastMetronomeMeasurePart = measurePart
            if (metronome) {
                val isStartOfMeasure = measurePart == 0
                metronomeSFX.sound.play(loop = false, pitch = if (isStartOfMeasure) 1.5f else 1.1f, volume = 1.25f)
            }
        }

        if (playState != PlayState.STOPPED
                && (beat >= duration || (main.preferences.getBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, false) && beat >= lastPoint))) {
            playState = PlayState.STOPPED
        }
    }

    override fun dispose() {
        music?.dispose()
        textureCache.values.forEach(Texture::dispose)
        textureCache.clear()
    }

}