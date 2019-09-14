package io.github.chrislo27.rhre3.soundsystem

import net.beadsproject.beads.data.Sample


open class BeadsAudio(channels: Int, sampleRate: Int) {

    val sample: Sample = Sample(0.0, channels, sampleRate.toFloat()).apply {
        clear()
    }

}