package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Music
import net.beadsproject.beads.ugens.SamplePlayer


class BeadsMusic(val audio: BeadsAudio) : Music {

    val player = GainedSamplePlayer(SamplePlayer(BeadsSoundSystem.audioContext, audio.sample).apply {
        killOnEnd = false
        pause(true)
    }).apply {
        addToContext()
    }

    override fun play() {
        player.player.start()
    }

    override fun stop() {
        player.player.pause(true)
        player.player.position = 0.0
    }

    override fun pause() {
        player.player.pause(true)
    }

    override fun getPosition(): Float {
        return player.player.position.toFloat()
    }

    override fun setPosition(seconds: Float) {
        player.player.position = seconds.toDouble()
    }

    override fun getVolume(): Float {
        return player.gain.gain
    }

    override fun setVolume(vol: Float) {
        player.gain.gain = vol.coerceIn(0f, 1f)
    }

    override fun dispose() {
        player.player.pause(true)
    }

    override fun isPlaying(): Boolean {
        return !player.player.isPaused
    }

    override fun update(delta: Float) {
    }
}