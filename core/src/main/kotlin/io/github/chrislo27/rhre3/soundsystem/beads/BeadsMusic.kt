package io.github.chrislo27.rhre3.soundsystem.beads

import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.soundsystem.Music
import net.beadsproject.beads.ugens.SamplePlayer


class BeadsMusic(val audio: BeadsAudio) : Music {

    val player: GainedSamplePlayer = GainedSamplePlayer(
            SamplePlayer(BeadsSoundSystem.audioContext, audio.sample).apply {
                killOnEnd = false
                pause(true)
            }) {}.apply {
        addToContext()
    }
    private val startOfSound: Float = run {
        val sample = audio.sample
        val array = FloatArray(sample.numChannels) { 0f }
        for (i in 0 until sample.numFrames) {
            sample.getFrame(i.toInt(), array)
            if (array.any { !MathUtils.isEqual(it, 0f, 0.0005f) }) {
                return@run sample.samplesToMs(i.toDouble()).toFloat() / 1000f
            }
        }
        -1f
    }

    override fun getStartOfSound(): Float {
        return startOfSound
    }

    override fun play() {
        player.addToContext()
        player.player.start()
    }

    override fun stop() {
        player.player.pause(true)
        player.player.reset()
    }

    override fun pause() {
        player.player.pause(true)
    }

    override fun getPosition(): Float {
        return player.player.position.toFloat() / 1000
    }

    override fun setPosition(seconds: Float) {
        player.player.position = seconds.toDouble() * 1000
    }

    override fun getVolume(): Float {
        return player.gain.gain
    }

    override fun setVolume(vol: Float) {
        player.gain.gain = vol.coerceAtLeast(0f)
    }

    override fun getPitch(): Float {
        return player.pitch.value
    }

    override fun setPitch(pitch: Float) {
        player.pitch.value = pitch
    }

    override fun dispose() {
//        player.player.pause(true)
//        player.player.kill()
    }

    override fun isPlaying(): Boolean {
        return !player.player.isPaused
    }

    override fun update(delta: Float) {
    }
}