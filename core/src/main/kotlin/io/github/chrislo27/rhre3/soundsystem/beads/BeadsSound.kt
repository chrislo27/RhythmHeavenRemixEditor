package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Sound
import net.beadsproject.beads.ugens.SamplePlayer
import java.util.concurrent.ConcurrentHashMap


class BeadsSound(val audio: BeadsAudio, val onDispose: (BeadsSound) -> Unit) : Sound {

    val players: MutableMap<Long, GainedSamplePlayer> = ConcurrentHashMap()

    private fun obtainPlayer(): Pair<Long, GainedSamplePlayer> {
        val id = BeadsSoundSystem.obtainSoundID()
        val samplePlayer = SamplePlayer(BeadsSoundSystem.audioContext, audio.sample)
        val result = id to GainedSamplePlayer(samplePlayer) { players.remove(id) }.also { gsp ->
            samplePlayer.apply {
                killOnEnd = true
            }
        }

        players.put(result.first, result.second)

        return result
    }

    override fun play(loop: Boolean, pitch: Float, rate: Float, volume: Float): Long {
        val result = obtainPlayer()
        val player = result.second.player

        player.loopType = if (loop) SamplePlayer.LoopType.LOOP_FORWARDS else SamplePlayer.LoopType.NO_LOOP_FORWARDS

        result.second.player.rateUGen.value = rate
        result.second.gain.gain = volume
        result.second.pitch.value = pitch
        result.second.addToContext()

        return result.first
    }

    override fun setPitch(id: Long, pitch: Float) {
        val player = players[id] ?: return
        player.pitch.value = pitch
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
        players.forEach { stop(it.key) }
        players.clear()
        onDispose.invoke(this)
    }
}