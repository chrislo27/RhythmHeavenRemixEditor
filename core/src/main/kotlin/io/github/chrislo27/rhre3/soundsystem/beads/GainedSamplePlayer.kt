package io.github.chrislo27.rhre3.soundsystem.beads

import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.*

class GainedSamplePlayer(val player: SamplePlayer, onKilled: () -> Unit) {

    val gain: Gain = Gain(BeadsSoundSystem.audioContext, player.outs, 1f)
    val pitch: Envelope = Envelope(BeadsSoundSystem.audioContext, 1f)
    val rate: Envelope = Envelope(BeadsSoundSystem.audioContext, 1f)
    val highPassFilter: BiquadFilter = BiquadFilter(BeadsSoundSystem.audioContext, player.outs, BiquadFilter.Type.BESSEL_HP).setFrequency(1500f)
    val lowPassFilter: BiquadFilter = BiquadFilter(BeadsSoundSystem.audioContext, player.outs, BiquadFilter.Type.BESSEL_LP).setFrequency(1000f)
    val tmpDistortGain: Gain = Gain(BeadsSoundSystem.audioContext, player.outs, 1.5f)

    private val toAdd
        get() = gain

    var doDistortion: Boolean = false
        set(value) {
            val old = field
            field = value
            if (old != value) {
                if (!value) {
                    // remove
                    gain.removeAllConnections(tmpDistortGain)
                    gain.addInput(player)
                } else {
                    // add
                    gain.removeAllConnections(player)
                    gain.addInput(tmpDistortGain)
                }
            }
        }

    init {
        highPassFilter.addInput(player)
        lowPassFilter.addInput(highPassFilter)
        tmpDistortGain.addInput(lowPassFilter)

        gain.addInput(player)
        if (player is GranularSamplePlayer) {
            player.setPitch(pitch)
            player.setRate(rate)
        } else {
            player.setPitch(object : Envelope(BeadsSoundSystem.audioContext, 1f) {
                override fun calculateBuffer() {
                    value = pitch.currentValue * rate.currentValue
                    super.calculateBuffer()
                }
            })
        }

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