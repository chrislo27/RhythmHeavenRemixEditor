package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Sound
import net.beadsproject.beads.ugens.GranularSamplePlayer
import net.beadsproject.beads.ugens.SamplePlayer
import java.util.concurrent.ConcurrentHashMap


class BeadsSound(val audio: BeadsAudio) : Sound {

    companion object {
        var useGranular: Boolean = false
    }

    val players: MutableMap<Long, GainedSamplePlayer> = ConcurrentHashMap()
    @Volatile
    private var disposed = false

    override val duration: Double
        get() = audio.sample.length

    private fun obtainPlayer(): Pair<Long, GainedSamplePlayer> {
        val id = BeadsSoundSystem.obtainSoundID()
        val samplePlayer = if (useGranular)
            GranularSamplePlayer(BeadsSoundSystem.audioContext, audio.sample)
        else SamplePlayer(BeadsSoundSystem.audioContext, audio.sample)
        val result = id to GainedSamplePlayer(samplePlayer) { players.remove(id) }.also { gsp ->
            samplePlayer.apply {
                killOnEnd = true
            }
        }

        players[result.first] = result.second

        return result
    }

    override fun play(loop: Boolean, pitch: Float, rate: Float, volume: Float, position: Double): Long {
        if (disposed)
            return -1L

        val (id, player) = obtainPlayer()

        player.player.loopType = if (loop) SamplePlayer.LoopType.LOOP_FORWARDS else SamplePlayer.LoopType.NO_LOOP_FORWARDS

        player.player.rateUGen.value = rate
        player.gain.gain = volume
        player.pitch.value = pitch
        player.rate.value = rate
        val dur = duration
        val normalizedPosition = (position.coerceIn(-dur, dur) % dur + dur) % dur
        player.player.position = normalizedPosition * 1000
        player.addToContext()

        return id
    }

    override fun setPitch(id: Long, pitch: Float) {
        val player = players[id] ?: return
        player.pitch.value = pitch
    }

    override fun setRate(id: Long, rate: Float) {
        val player = players[id] ?: return
        player.rate.value = rate
    }

    override fun setVolume(id: Long, vol: Float) {
        val player = players[id] ?: return
        player.gain.gain = vol
    }

    override fun stop(id: Long) {
        val player = players[id] ?: return
        players.remove(id)

        player.player.kill()
    }

    override fun dispose() {
        if (disposed)
            return

        disposed = true
        players.forEach { stop(it.key) }
        players.clear()
        BeadsSoundSystem.disposeSound(this)
    }
}