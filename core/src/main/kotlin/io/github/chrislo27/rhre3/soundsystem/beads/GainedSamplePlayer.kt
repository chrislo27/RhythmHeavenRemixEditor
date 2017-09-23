package io.github.chrislo27.rhre3.soundsystem.beads

import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.Envelope
import net.beadsproject.beads.ugens.Gain
import net.beadsproject.beads.ugens.SamplePlayer

class GainedSamplePlayer(val player: SamplePlayer, onKilled: () -> Unit) {

    val gain = Gain(
            BeadsSoundSystem.audioContext, player.outs, 1f)
    val pitch = Envelope(
            BeadsSoundSystem.audioContext, 1f)

    private val toAdd
        get() = gain

    init {
        gain.addInput(player)
        player.setPitch(pitch)

        player.killListener = object : Bead() {
            override fun messageReceived(message: Bead?) {
                onKilled.invoke()
                toAdd.kill()
            }
        }
    }

    fun addToContext() {
        val context = BeadsSoundSystem.audioContext
        if (toAdd !in context.out.connectedInputs) {
            context.out.addInput(toAdd)
        }
    }

}