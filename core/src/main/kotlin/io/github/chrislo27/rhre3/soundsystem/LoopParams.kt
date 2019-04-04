package io.github.chrislo27.rhre3.soundsystem

import net.beadsproject.beads.ugens.SamplePlayer


data class LoopParams(val loopType: SamplePlayer.LoopType, val startPoint: Double, val endPoint: Double) {
    companion object {
        val NO_LOOP_FORWARDS = LoopParams(SamplePlayer.LoopType.NO_LOOP_FORWARDS, 0.0, 0.0)
        val LOOP_FORWARDS_ENTIRE = LoopParams(SamplePlayer.LoopType.LOOP_FORWARDS, 0.0, 0.0)
    }
}

