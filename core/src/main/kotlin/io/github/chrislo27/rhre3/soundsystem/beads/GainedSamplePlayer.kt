package io.github.chrislo27.rhre3.soundsystem.beads

import net.beadsproject.beads.ugens.Envelope
import net.beadsproject.beads.ugens.Gain
import net.beadsproject.beads.ugens.SamplePlayer

class GainedSamplePlayer(val player: SamplePlayer) {

    val gain = Gain(
            BeadsSoundSystem.audioContext, player.outs, 1f)
    val pitch = Envelope(
            BeadsSoundSystem.audioContext, 1f)

    init {
        gain.addInput(player)
//            pitch.addInput(gain)

        player.setPitch(pitch)
    }

    fun addToContext() {
        val toAdd = gain
        val context = BeadsSoundSystem.audioContext
        if (toAdd !in context.out.connectedInputs) {
            context.out.addInput(toAdd)
        }
    }

}