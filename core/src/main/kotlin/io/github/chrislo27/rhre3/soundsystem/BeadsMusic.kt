package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable
import net.beadsproject.beads.ugens.SamplePlayer


class BeadsMusic(val audio: BeadsAudio) : Disposable {

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

    fun getStartOfSound(): Float {
        return startOfSound
    }

    fun play() {
        player.addToContext()
        player.player.start()
    }

    fun stop() {
        player.player.pause(true)
        player.player.reset()
    }

    fun pause() {
        player.player.pause(true)
    }

    fun getPosition(): Float {
        return player.player.position.toFloat() / 1000
    }

    fun setPosition(seconds: Float) {
        player.player.position = seconds.toDouble() * 1000
    }

    fun getVolume(): Float {
        return player.gain.gain
    }

    fun setVolume(vol: Float) {
        player.gain.gain = vol.coerceAtLeast(0f)
    }

    fun getPitch(): Float {
        return player.pitch.value
    }

    fun setPitch(pitch: Float) {
        player.pitch.value = pitch
    }

    override fun dispose() {
//        player.player.pause(true)
//        player.player.kill()
    }

    fun isPlaying(): Boolean {
        return !player.player.isPaused
    }

    fun update(delta: Float) {
    }
}