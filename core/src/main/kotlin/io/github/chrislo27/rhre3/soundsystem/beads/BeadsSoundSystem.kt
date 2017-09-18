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

object BeadsSoundSystem : SoundSystem() {

    val audioContext: AudioContext = AudioContext(JavaSoundAudioIO())
    @Volatile
    var currentSoundID: Long = 0
        private set

    fun obtainSoundID(): Long {
        return currentSoundID++
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun stop() {
    }

    fun newAudio(handle: FileHandle): BeadsAudio {
        val music = Gdx.audio.newMusic(handle) as OpenALMusic
        val beadsAudio = BeadsAudio(music.channels, music.rate)
        val sampleData: Array<FloatArray>

        music.reset()

        sampleData = run {
            val BUFFER_SIZE = 4096 * 4
            val audioBytes = ByteArray(BUFFER_SIZE)
            val tempFile = File.createTempFile("rhre3-data-${System.currentTimeMillis()}", "tmp").apply {
                deleteOnExit()
            }
            val fileOutStream = FileOutputStream(tempFile)

            while (true) {
                val length = music.read(audioBytes)
                if (length <= 0)
                    break

                fileOutStream.write(audioBytes, 0, length)
            }
            StreamUtils.closeQuietly(fileOutStream)

            // Copied from Beads JavaSoundAudioFile source code
            val data = tempFile.readBytes()
            val nFrames = data.size / (2 * music.channels)

            // Copy and de-interleave entire data
            val sampleData = Array(music.channels) { FloatArray(nFrames) }
            val interleaved = FloatArray((music.channels * nFrames))
            AudioUtils.byteToFloat(interleaved, data, false)
            System.gc()
            AudioUtils.deinterleave(interleaved, music.channels, nFrames, sampleData)

            try {
                tempFile.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            sampleData
        }

        music.dispose()


        val sample = beadsAudio.sample
        sample.resize(sampleData[0].size.toLong())
        sample.putFrames(0, sampleData)

        return beadsAudio
    }

    override fun newSound(handle: FileHandle): Sound {
        TODO()
    }

    override fun newMusic(handle: FileHandle): Music {
        TODO()
    }

    override fun onSet() {
        super.onSet()
    }
}