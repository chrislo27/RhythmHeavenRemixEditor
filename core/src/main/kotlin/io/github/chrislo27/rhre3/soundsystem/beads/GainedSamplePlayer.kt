package io.github.chrislo27.rhre3.soundsystem.beads

import net.beadsproject.beads.ugens.Envelope
import net.beadsproject.beads.ugens.Gain
import net.beadsproject.beads.ugens.SamplePlayer

class GainedSamplePlayer(val player: SamplePlayer) {

    val gain = Gain(
            BeadsSoundSystem.audioContext, 1, 1f)
    val pitch = Envelope(
            BeadsSoundSystem.audioContext, 1f)

    init {
        gain.addInput(player)
//            pitch.addInput(gain)

        player.setPitch(pitch)
    }

    fun addToContext() {
        BeadsSoundSystem.audioContext.out.addInput(player)
    }

}