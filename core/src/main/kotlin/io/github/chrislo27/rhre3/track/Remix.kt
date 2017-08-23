package io.github.chrislo27.rhre3.track

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.oopsies.ActionHistory
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.tempo.Tempos
import io.github.chrislo27.rhre3.track.music.MusicData
import io.github.chrislo27.rhre3.track.music.MusicVolumes
import io.github.chrislo27.rhre3.track.timesignature.TimeSignatures
import io.github.chrislo27.rhre3.tracker.TrackerContainer
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.registry.AssetRegistry
import kotlin.properties.Delegates


class Remix(val camera: OrthographicCamera, val editor: Editor)
    : ActionHistory<Remix>(), Disposable {

    companion object {
        /*

        To correctly do persistent data:

        * Update tracker blocks of code when new trackers are added
        * Update when statement in fromJson

         */

        fun toJson(remix: Remix): ObjectNode {
            val tree = JsonHandler.OBJECT_MAPPER.createObjectNode()

            remix.apply {
                tree.put("version", RHRE3.VERSION.toString())

                tree.put("playbackStart", playbackStart)
                tree.put("musicStartSec", musicStartSec)

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

        fun fromJson(tree: ObjectNode, remix: Remix): Remix {
            remix.playbackStart = tree["playbackStart"]?.floatValue() ?: 0f
            remix.musicStartSec = tree["musicStartSec"]?.floatValue() ?: 0f

            // entities
            val entitiesArray = tree["entities"] as ArrayNode
            entitiesArray.filterIsInstance<ObjectNode>()
                    .filter { it.has("type") }
                    .forEach { node ->
                        val type = node["type"].asText(null) ?: return@forEach

                        val entity: Entity = when (type) {
                            "model" -> {
                                val datamodelID = node[ModelEntity.JSON_DATAMODEL].asText(null)
                                ?: error("Malformed model entiy object: missing datamodel ID (${ModelEntity.JSON_DATAMODEL})")
                                GameRegistry.data.objectMap[datamodelID]?.createEntity(remix)
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

            return remix
        }
    }

    val main: RHRE3Application
        get() = editor.main

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
    var currentSubtitle: String = ""

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
                    currentSubtitle = ""
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
                            if (it.bounds.x + it.bounds.width < beat) {
                                it.playbackCompletion = PlaybackCompletion.FINISHED
                            } else {
                                it.playbackCompletion = PlaybackCompletion.WAITING
                            }
                        }

                        lastTickBeat = Math.ceil(playbackStart - 1.0).toInt()

                        if (editor.stage.tapalongStage.visible) {
                            editor.stage.tapalongStage.reset()
                        }

                        currentSubtitle = ""
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
        music?.music?.volume = if (isMusicMuted) 0f else musicVolumes.getPercentageVolume(beat)
    }

    private fun seekMusic() {
        val music = music ?: return
        musicSeeking = true
        music.music.position = seconds - musicStartSec
        musicSeeking = false
    }

    private fun computeDuration() {
        duration = entities.firstOrNull { it is EndEntity }?.bounds?.x ?: Float.POSITIVE_INFINITY
    }

    fun getLastPoint(): Float {
        if (entities.isEmpty())
            return 0f
        return if (entities.isNotEmpty() && entities.any { it is EndEntity }) {
            entities.first { it is EndEntity }.bounds.x
        } else {
            val last = entities.maxBy { it.bounds.x + it.bounds.width }!!
            last.bounds.x + last.bounds.y
        }
    }

    fun entityUpdate(entity: Entity) {
        if (entity.playbackCompletion == PlaybackCompletion.WAITING) {
            if (beat in entity.bounds.x..(entity.bounds.x + entity.bounds.width)) {
                entity.playbackCompletion = PlaybackCompletion.PLAYING
                entity.onStart()
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
        if (playState != PlayState.PLAYING)
            return

        val music: MusicData? = music

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

                val musicVolume = musicVolumes.getPercentageVolume(beat)
                if (musicVolume != music.music.volume) {
                    music.music.volume = musicVolume
                }
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

        if (beat >= duration && playState != PlayState.STOPPED) {
            playState = PlayState.STOPPED
        }
    }

    override fun dispose() {
        music?.dispose()
    }
}