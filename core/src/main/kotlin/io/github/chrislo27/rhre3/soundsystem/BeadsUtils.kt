package io.github.chrislo27.rhre3.soundsystem

import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.core.IOAudioFormat
import net.beadsproject.beads.core.io.JavaSoundAudioIO
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer


/**
 * Gets all the averaged values for the current out buffer of the context.
 */
fun AudioContext.getValues(buffer: FloatArray) {
    if (buffer.size != this.bufferSize)
        error("Buffer size incorrect, got ${buffer.size}, should be ${this.bufferSize}")

    for (channel in 0 until out.ins) {
        for (i in buffer.indices) {
            if (channel == 0)
                buffer[i] = 0f
            buffer[i] += out.getValue(channel, i)

            if (i == buffer.size - 1) {
                buffer[i] = buffer[i] / out.ins
            }
        }
    }
}

/**
 * Gets all the values for the given channel for the current out buffer of the context.
 */
fun AudioContext.getValues(buffer: FloatArray, channel: Int) {
    if (buffer.size != this.bufferSize)
        error("Buffer size incorrect, got ${buffer.size}, should be ${this.bufferSize}")

    for (i in buffer.indices) {
        buffer[i] = out.getValue(channel, i)
    }
}

fun IOAudioFormat.toJavaAudioFormat(): AudioFormat = AudioFormat(sampleRate, bitDepth, outputs, signed, bigEndian)

fun JavaSoundAudioIO.selectMixer(mixer: Mixer) {
    val clazz = JavaSoundAudioIO::class.java
    clazz.getDeclaredField("mixer").also { mxr ->
        mxr.isAccessible = true
        mxr.set(this@selectMixer, mixer)
    }
    println("JavaSoundAudioIO: Chosen mixer is ${mixer.mixerInfo.name}.")
}
