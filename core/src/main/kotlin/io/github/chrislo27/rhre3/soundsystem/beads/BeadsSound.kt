package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Sound
import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.SamplePlayer


class BeadsSound(val audio: BeadsAudio) : Sound {

    private val players: MutableMap<Long, SamplePlayer> = mutableMapOf()

    private fun obtainPlayer(): Pair<Long, SamplePlayer> {
        val id = BeadsSoundSystem.obtainSoundID()
        val result = id to SamplePlayer(BeadsSoundSystem.audioContext, audio.sample).apply {
            killOnEnd = true
            killListener = object : Bead() {
                override fun messageReceived(message: Bead?) {
                    if (message == this) {
                        players.remove(id)
                    }
                }
            }
        }

        players.put(result.first, result.second)

        return result
    }

    override fun play(loop: Boolean, pitch: Float, volume: Float): Long {
        val result = obtainPlayer()
        val player = result.second

        player.loopType = if (loop) SamplePlayer.LoopType.LOOP_FORWARDS else SamplePlayer.LoopType.NO_LOOP_FORWARDS

        // TODO play

        return result.first
    }

    override fun setPitch(id: Long, pitch: Float) {
        // TODO
    }

    override fun setVolume(id: Long, vol: Float) {
        // TODO set gain
    }

    override fun stop(id: Long) {
        val player = players[id] ?: return
        players.remove(id)

        player.kill()
    }

    override fun dispose() {
        players.forEach { stop(it.key) }
        players.clear()
    }
}