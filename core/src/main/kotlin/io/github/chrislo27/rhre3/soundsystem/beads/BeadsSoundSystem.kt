package io.github.chrislo27.rhre3.soundsystem.beads

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.util.FastSeekingMusic
import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.core.AudioUtils
import net.beadsproject.beads.core.io.JavaSoundAudioIO

object BeadsSoundSystem : SoundSystem() {

    val audioContext: AudioContext = AudioContext(JavaSoundAudioIO())
    @Volatile
    var currentSoundID: Long = 0
        private set

    fun obtainSoundID(): Long {
        return currentSoundID++
    }

    fun newAudio(handle: FileHandle): BeadsAudio {
        val music = Gdx.audio.newMusic(handle) as OpenALMusic
        val beadsAudio = BeadsAudio(music.channels, music.rate)
        val sampleData: Array<FloatArray>

        // Copied from Beads JavaSoundAudioFile source code
        sampleData = run {
            val BUFFERSIZE = 4096
            val audioBytes = ByteArray(BUFFERSIZE)
            var sampleBufferSize = 4096
            var data = ByteArray(sampleBufferSize)
            var bytesRead: Int = 0
            var totalBytesRead = 0

            fun readAndSetBytesRead(): Int {
                bytesRead = music.read(audioBytes)
                return bytesRead
            }

            while (readAndSetBytesRead() != -1) {
                // resize buf if necessary
                if (bytesRead > sampleBufferSize - totalBytesRead) {
                    sampleBufferSize = Math.max(sampleBufferSize * 2, sampleBufferSize + bytesRead)
                    val newBuf = ByteArray(sampleBufferSize)
                    System.arraycopy(data, 0, newBuf, 0, data.size)
                    data = newBuf
                }
                System.arraycopy(audioBytes, 0, data, totalBytesRead, bytesRead)
                totalBytesRead += bytesRead
            }

            // resize buf to proper length if necessary
            if (sampleBufferSize > totalBytesRead) {
                sampleBufferSize = totalBytesRead
                val newBuf = ByteArray(sampleBufferSize)
                System.arraycopy(data, 0, newBuf, 0, sampleBufferSize)
                data = newBuf
            }
            val nFrames = sampleBufferSize / (2 * music.channels)

            // Copy and de-interleave entire data
            val sampleData = Array(music.channels) { FloatArray(nFrames) }
            val interleaved = FloatArray((music.channels * nFrames))
            AudioUtils.byteToFloat(interleaved, data, true)
            AudioUtils.deinterleave(interleaved, music.channels, nFrames, sampleData)

            sampleData
        }

        music.dispose()

        val sample = beadsAudio.sample
        sample.resizeWithZeros(sampleData[0].size.toLong())
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
        LazySound.soundFactory = BeadsSoundSystem::newSound
        FastSeekingMusic.musicFactory = BeadsSoundSystem::newMusic
    }
}