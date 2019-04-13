package io.github.chrislo27.rhre3.soundsystem.beads

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.rhre3.soundsystem.Music
import io.github.chrislo27.rhre3.soundsystem.Sound
import io.github.chrislo27.rhre3.util.err.MusicTooLargeException
import io.github.chrislo27.rhre3.util.err.MusicWayTooLargeException
import io.github.chrislo27.toolboks.Toolboks
import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.core.AudioUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

object BeadsSoundSystem {

    private val realtimeAudioContext: AudioContext = createAudioContext()
    private val nonrealtimeAudioContext: AudioContext = createAudioContext()

    val audioContext: AudioContext
        get() = if (isRealtime) realtimeAudioContext else nonrealtimeAudioContext
    @Volatile
    var currentSoundID: Long = 1L
        private set

    @Volatile
    var isRealtime: Boolean = true

    private val sounds: MutableList<BeadsSound> = CopyOnWriteArrayList()

    var sampleArray: FloatArray = FloatArray(AudioContext.DEFAULT_BUFFER_SIZE)
        private set
        get() {
            if (field.size != audioContext.bufferSize) {
                field = FloatArray(audioContext.bufferSize)
            }

            return field
        }

    private fun createAudioContext(): AudioContext =
            AudioContext(DaemonJavaSoundAudioIO().apply {
                val index = AudioSystem.getMixerInfo().toList().indexOfFirst {
                    !it.name.startsWith("Port ") || it.name.contains("Primary Sound Driver")
                }
                if (index != -1) {
                    selectMixer(index)
                } else {
                    val ioAudioFormat = context.audioFormat
                    val audioFormat = AudioFormat(ioAudioFormat.sampleRate, ioAudioFormat.bitDepth,
                                                  ioAudioFormat.outputs,
                                                  ioAudioFormat.signed, ioAudioFormat.bigEndian)
                    val info = DataLine.Info(SourceDataLine::class.java,
                                             audioFormat)

                    val otherIndex = AudioSystem.getMixerInfo().toList().indexOfFirst {
                        val mixer = AudioSystem.getMixer(it)
                        try {
                            mixer.getLine(info)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }

                    if (otherIndex != -1) {
                        selectMixer(otherIndex)
                    } else {
                    }
                }
            })

    init {
        audioContext.start()
    }

    fun obtainSoundID(): Long {
        return currentSoundID++
    }

    fun resume() {
        audioContext.out.pause(false)
    }

    fun pause() {
        audioContext.out.pause(true)
    }

    fun stop() {
        audioContext.out.pause(true)
        audioContext.out.clearInputConnections()
    }

    fun dispose() {
        stop()
        realtimeAudioContext.stop()
        nonrealtimeAudioContext.stop()
    }

    fun newAudio(handle: FileHandle): BeadsAudio {
        val music = Gdx.audio.newMusic(handle) as OpenALMusic
        val beadsAudio = BeadsAudio(music.channels, music.rate)

        music.reset()

        run {
            val BUFFER_SIZE = 4096 * 4
            val audioBytes = ByteArray(BUFFER_SIZE)
            val tempFile = File.createTempFile("rhre3-data-${System.currentTimeMillis()}-${System.nanoTime() % 1_000_000}", ".tmp").apply {
                deleteOnExit()
            }
            val fileOutStream = FileOutputStream(tempFile)
            val sample = beadsAudio.sample
            var currentLength: Long = 0L
            val overflowed: Boolean

            while (true) {
                val length = music.read(audioBytes)
                if (length <= 0) {
                    overflowed = length < -1
                    if (overflowed) {
                        Toolboks.LOGGER.info("Potential overflow when reading music: got $length as length")
                    }
                    break
                }

                currentLength += length

                fileOutStream.write(audioBytes, 0, length)
            }
            StreamUtils.closeQuietly(fileOutStream)

            val bufStream = tempFile.inputStream()
            val length = currentLength
            Toolboks.LOGGER.info("Loading audio ${handle.name()} - $length bytes")
            if (length > Int.MAX_VALUE || overflowed)
                throw MusicWayTooLargeException(length)

            val nFrames = length / (2 * music.channels)
            try {
                sample.resize(nFrames)
            } catch (oome: OutOfMemoryError) {
                oome.printStackTrace()
                // 32 bit float per sample
                throw MusicTooLargeException(nFrames * music.channels * 4, oome)
            }
            val interleaved = FloatArray(music.channels * (BUFFER_SIZE / (2 * music.channels)))
            val sampleData = Array(music.channels) { FloatArray(interleaved.size / music.channels) }

            var currentFrame = 0
            while (true) {
                val len = bufStream.read(audioBytes)
                if (len <= 0)
                    break

                val framesOfDataRead = len / (2 * music.channels)

                AudioUtils.byteToFloat(interleaved, audioBytes, false, len / 2) // 2 bytes per 16 bit float
                AudioUtils.deinterleave(interleaved, music.channels, framesOfDataRead, sampleData)

                sample.putFrames(currentFrame, sampleData, 0, framesOfDataRead)

                currentFrame += framesOfDataRead
            }
            StreamUtils.closeQuietly(bufStream)

            try {
                tempFile.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        music.dispose()

        return beadsAudio
    }

    fun newSound(handle: FileHandle): Sound {
        return BeadsSound(newAudio(handle)).apply {
            sounds += this
        }
    }

    fun newMusic(handle: FileHandle): Music {
        return BeadsMusic(newAudio(handle))
    }

    fun disposeSound(sound: BeadsSound) {
        sounds -= sound
    }
}