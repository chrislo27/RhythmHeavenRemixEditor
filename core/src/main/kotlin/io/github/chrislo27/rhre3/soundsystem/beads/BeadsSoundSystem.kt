package io.github.chrislo27.rhre3.soundsystem.beads

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.rhre3.soundsystem.Music
import io.github.chrislo27.rhre3.soundsystem.Sound
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.core.AudioUtils
import net.beadsproject.beads.core.io.JavaSoundAudioIO
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

object BeadsSoundSystem : SoundSystem() {

    override val id: String = "beads"
    val audioContext: AudioContext = AudioContext(JavaSoundAudioIO())
    @Volatile
    var currentSoundID: Long = 0
        private set

    private val sounds: MutableList<BeadsSound> = CopyOnWriteArrayList()

    fun obtainSoundID(): Long {
        return currentSoundID++
    }

    override fun resume() {
        audioContext.out.pause(false)
    }

    override fun pause() {
        audioContext.out.pause(true)
    }

    override fun stop() {
        audioContext.out.pause(true)
        audioContext.out.clearInputConnections()
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

            while (true) {
                val length = music.read(audioBytes)
                if (length <= 0)
                    break

                fileOutStream.write(audioBytes, 0, length)
            }
            StreamUtils.closeQuietly(fileOutStream)

            val bufStream = tempFile.inputStream()
            val length = tempFile.length()
            if (length >= Int.MAX_VALUE)
                throw OutOfMemoryError("File too big")

            val nFrames = length / (2 * music.channels)
            sample.resize(nFrames)
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

    override fun newSound(handle: FileHandle): Sound {
        return BeadsSound(newAudio(handle)).apply {
            sounds += this
        }
    }

    override fun newMusic(handle: FileHandle): Music {
        return BeadsMusic(newAudio(handle))
    }

    override fun onSet() {
        super.onSet()

        // FIXME
        audioContext.start()
    }
}