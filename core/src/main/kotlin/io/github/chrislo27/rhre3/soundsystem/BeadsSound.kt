package io.github.chrislo27.rhre3.soundsystem

import net.beadsproject.beads.ugens.GranularSamplePlayer
import net.beadsproject.beads.ugens.SamplePlayer
import net.beadsproject.beads.ugens.Static
import java.util.concurrent.ConcurrentHashMap


class BeadsSound(val audio: BeadsAudio) {

    companion object {
        var useGranular: Boolean = false
    }

    private val players: MutableMap<Long, GainedSamplePlayer> = ConcurrentHashMap()
    @Volatile
    private var disposed = false

    val duration: Double
        get() = audio.sample.length / 1000

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

    fun play(loop: Boolean, pitch: Float, rate: Float, volume: Float, position: Double): Long {
        return playWithLoop(pitch, rate, volume, position, if (loop) LoopParams.LOOP_FORWARDS_ENTIRE.copy(endPoint = duration) else LoopParams.NO_LOOP_FORWARDS)
    }

    fun playWithLoop(pitch: Float, rate: Float, volume: Float, position: Double, loopParams: LoopParams): Long {
        if (disposed)
            return -1L

        val (id, player) = obtainPlayer()

        player.player.loopType = loopParams.loopType

        player.player.rateUGen.value = rate
        player.gain.gain = volume
        player.pitch.value = pitch
        player.rate.value = rate
        val dur = duration
        val clampedPosition = (position.coerceIn(-dur, dur))
        player.player.position = (if (clampedPosition < 0) (dur + clampedPosition) else clampedPosition) * 1000
        if (loopParams.startPoint > 0) {
            player.player.setLoopStart(Static(player.player.context, loopParams.startPoint.toFloat() * 1000))
        }
        if (loopParams.endPoint > 0) {
            player.player.setLoopEnd(Static(player.player.context, loopParams.endPoint.toFloat() * 1000))
        }
        player.addToContext()

        return id
    }

    fun setPitch(id: Long, pitch: Float) {
        val player = players[id] ?: return
        player.pitch.value = pitch
    }

    fun setRate(id: Long, rate: Float) {
        val player = players[id] ?: return
        player.rate.value = rate
    }

    fun setVolume(id: Long, vol: Float) {
        val player = players[id] ?: return
        player.gain.gain = vol
    }

    fun stop(id: Long) {
        val player = players[id] ?: return
        players.remove(id)

        player.player.kill()
    }

    fun dispose() {
        if (disposed)
            return

        disposed = true
        players.forEach { stop(it.key) }
        players.clear()
    }

    fun getPlayer(id: Long): GainedSamplePlayer? = players[id]
}