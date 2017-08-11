package io.github.chrislo27.rhre3.track

import io.github.chrislo27.rhre3.oopsies.ActionHistory
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.tempo.Tempos
import io.github.chrislo27.rhre3.track.music.MusicData
import io.github.chrislo27.rhre3.track.music.MusicVolumes
import io.github.chrislo27.rhre3.tracker.TrackerContainer
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Remix(val camera: OrthographicCamera, val editor: Editor) : ActionHistory<Remix>() {

    val main: RHRE3Application
        get() = editor.main

    val entities: MutableList<Entity> = mutableListOf()
    val trackers: MutableList<TrackerContainer<*>> = mutableListOf()
    val tempos: Tempos = run {
        val t = Tempos()
        trackers += t
        t
    }
    val musicVolumes: MusicVolumes = run {
        val mv = MusicVolumes()
        trackers += mv
        mv
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
    private var lastMusicPosition: Float = -1f

    var playState: PlayState = PlayState.STOPPED
        set(value) {
            val old = field
            val music = music
            field = value
            when (field) {
                PlayState.STOPPED -> {
                    AssetRegistry.stopAllSounds()
                    music?.music?.stop()
                }
                PlayState.PAUSED -> {
                    AssetRegistry.pauseAllSounds()
                    music?.music?.pause()
                }
                PlayState.PLAYING -> {
                    lastMusicPosition = -1f
                    AssetRegistry.resumeAllSounds()
                    if (old == PlayState.STOPPED) {
                        seconds = tempos.beatsToSeconds(playbackStart)
                        entities.forEach {
                            if (it.bounds.x + it.bounds.width < beat) {
                                it.playbackCompletion = PlaybackCompletion.FINISHED
                            } else {
                                it.playbackCompletion = PlaybackCompletion.WAITING
                            }
                        }
                    }
                    if (music != null) {
                        music.music.play()
                        setMusicVolume()
                        music.music.position = seconds - musicStartSec
                    }
                }
            }
        }

    private fun setMusicVolume() {
        music?.music?.volume = musicVolumes.getVolume(beat)
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
            val oldPosition = lastMusicPosition
            val position = music.music.position
            lastMusicPosition = position

            if (oldPosition != position) {
                seconds = position
            }
        }

        entities.forEach { entity ->
            if (entity.playbackCompletion != PlaybackCompletion.FINISHED) {
                entityUpdate(entity)
            }
        }
    }

}