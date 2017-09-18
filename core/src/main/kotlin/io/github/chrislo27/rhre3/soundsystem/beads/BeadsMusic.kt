package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Music
import net.beadsproject.beads.ugens.SamplePlayer


class BeadsMusic(val audio: BeadsAudio) : Music {

    val player = SamplePlayer(BeadsSoundSystem.audioContext, audio.sample).apply {
        killOnEnd = false
    }

    override fun play() {
        // TODO
    }

    override fun stop() {
        // TODO
    }

    override fun getPosition(): Float {
        return player.position.toFloat()
    }

    override fun setPosition(seconds: Float) {
        player.position = seconds.toDouble()
    }

    override fun dispose() {
        player.kill()
    }
}