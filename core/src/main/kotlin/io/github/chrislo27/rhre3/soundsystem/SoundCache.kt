package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.RHRE3
import java.io.File
import java.util.concurrent.ConcurrentHashMap


object SoundCache {

    data class Derivative(val tempoPercent: Float, val pitchSemitones: Float, val ratePercent: Float = 0f) {
        fun isUnmodified(): Boolean = tempoPercent == 0f && pitchSemitones == 0f && ratePercent == 0f
    }

    data class DerivativeAudio(val audio: BeadsAudio, val quick: Boolean)
    data class AudioPointer(val originalFile: File, val audio: BeadsAudio) {
        val wavFile: File by lazy {
            val wavFile = File.createTempFile("rhre-lampshade-derivative-", ".wav").apply {
                deleteOnExit()
            }
            if (!wavFile.exists() || wavFile.length() == 0L) {
                // Save the wav version of the original to this file
                wavFile.createNewFile()
                audio.sample.write(wavFile.canonicalPath)
            }
            wavFile
        }
        private val derivativeAudioCache: MutableMap<Derivative, DerivativeAudio> = ConcurrentHashMap()

        fun derivativeOf(derivative: Derivative, quick: Boolean): BeadsAudio {
            if (derivative.isUnmodified()) {
                return audio
            }
            val cachedDerivAudio = derivativeAudioCache.getOrPut(derivative) {
                createDerivativeAudio(derivative, quick)
            }
            return (if (!quick && cachedDerivAudio.quick) {
                // If non-quick audio is requested, then the derivative audio must be non-quick
                // If quick audio is requested, non-quick audio is okay if it was already in the cache
                // This prioritizes quality
                val newDeriv: DerivativeAudio = createDerivativeAudio(derivative, false)
                derivativeAudioCache[derivative] = newDeriv
                newDeriv
            } else cachedDerivAudio).audio
        }

        fun derivativeOf(tempoPercent: Float, pitchSemitones: Float, ratePercent: Float = 0f, quick: Boolean = false): BeadsAudio {
            return derivativeOf(Derivative(tempoPercent, pitchSemitones, ratePercent), quick)
        }

        private fun createDerivativeAudio(derivative: Derivative, quick: Boolean): DerivativeAudio {
            val originalWav: File = wavFile
            val tmpFile: File = File.createTempFile("rhre-lampshade-derivative-gen-", ".wav").apply {
                deleteOnExit()
            }
            SoundStretch.processStreams(RHRE3.SOUNDSTRETCH_FOLDER.file(), originalWav, tmpFile,
                                        derivative.tempoPercent, derivative.pitchSemitones, derivative.ratePercent, quick)
            val moddedAudio: BeadsAudio = BeadsSoundSystem.newAudio(FileHandle(tmpFile)).apply {
                tmpFile.delete()
            }
            return DerivativeAudio(moddedAudio, quick)
        }
    }

    private val cache: MutableMap<File, AudioPointer> = ConcurrentHashMap()

    fun unload(f: File) {
        cache.remove(f)
    }

    fun clean(keepThese: List<File> = listOf()) {
        (cache.keys.toSet() - keepThese.toSet()).forEach { f ->
            unload(f)
        }
    }

    fun getOrLoad(file: File): AudioPointer {
        return cache.getOrPut(file) { AudioPointer(file, BeadsSoundSystem.newAudio(FileHandle(file))) }
    }

}