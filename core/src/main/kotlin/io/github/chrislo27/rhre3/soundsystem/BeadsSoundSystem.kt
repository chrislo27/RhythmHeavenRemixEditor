package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALMusic
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.toolboks.Toolboks
import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.core.AudioUtils
import net.beadsproject.beads.core.IOAudioFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.sound.sampled.*

object BeadsSoundSystem {
    
    val ioAudioFormat: IOAudioFormat = IOAudioFormat(44_100f, 16, 2, 2, true, true)
    val audioFormat: AudioFormat = ioAudioFormat.toJavaAudioFormat()
    val datalineInfo: DataLine.Info = DataLine.Info(SourceDataLine::class.java, audioFormat)
    val supportedMixers: List<Mixer> = AudioSystem.getMixerInfo().map { AudioSystem.getMixer(it) }.filter { mixer ->
        try {
            // Attempt to get the line. If it is not supported it will throw an exception.
            mixer.getLine(datalineInfo)
            Toolboks.LOGGER.debug("Mixer $mixer is compatible for outputting.")
            true
        } catch (e: Exception) {
            Toolboks.LOGGER.debug("Mixer $mixer is NOT compatible for outputting!")
            false
        }
    }

    var currentMixer: Mixer = getDefaultMixer()
        private set
    private var realtimeAudioContext: AudioContext = createAudioContext(currentMixer)
    private var nonrealtimeAudioContext: AudioContext = createAudioContext(currentMixer)

    val audioContext: AudioContext
        get() = if (isRealtime) realtimeAudioContext else nonrealtimeAudioContext
    @Volatile
    var currentSoundID: Long = 1L
        private set

    @Volatile
    var isRealtime: Boolean = true

    var sampleArray: FloatArray = FloatArray(AudioContext.DEFAULT_BUFFER_SIZE)
        private set
        get() {
            if (field.size != audioContext.bufferSize) {
                field = FloatArray(audioContext.bufferSize)
            }

            return field
        }

    /**
     * DANGEROUS OPERATION. Forcibly resets [realtimeAudioContext] and [nonrealtimeAudioContext] and
     * reinitializes them.
     */
    fun regenerateAudioContexts(mixer: Mixer) {
        currentMixer = mixer
        realtimeAudioContext = createAudioContext(mixer)
        nonrealtimeAudioContext = createAudioContext(mixer)
        audioContext.start()
    }
    
    fun getDefaultMixer(): Mixer {
        val allMixers = supportedMixers
        val first = allMixers.firstOrNull {
            val name = it.mixerInfo.name
            !name.startsWith("Port ") || name.contains("Primary Sound Driver")
        }
        return first ?: allMixers.first()
    }
    
    fun createAudioContext(mixer: Mixer): AudioContext =
            AudioContext(DaemonJavaSoundAudioIO().apply {
                selectMixer(mixer)
            }, AudioContext.DEFAULT_BUFFER_SIZE, ioAudioFormat)

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
            val tempFile = File.createTempFile("rhre3-data-${System.currentTimeMillis()}", ".tmp").apply {
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
//                throw MusicWayTooLargeException(length)
                error("Audio is too large (${length} bytes)")

            val nFrames = length / (2 * music.channels)
//            try {
            sample.resize(nFrames)
//            } catch (oome: OutOfMemoryError) {
//                oome.printStackTrace()
//                // 32 bit float per sample
//                throw MusicTooLargeException(nFrames * music.channels * 4, oome)
//            }
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

    fun newSound(audio: BeadsAudio): BeadsSound {
        return BeadsSound(audio)
    }

    fun newMusic(audio: BeadsAudio): BeadsMusic {
        return BeadsMusic(audio)
    }

    fun newSound(handle: FileHandle): BeadsSound {
        return newSound(newAudio(handle))
    }

    fun newMusic(handle: FileHandle): BeadsMusic {
        return newMusic(newAudio(handle))
    }
}