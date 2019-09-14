package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.RHRE3
import java.io.File
import java.util.concurrent.ConcurrentHashMap


object SoundCache {
    
    data class Derivative(val originalFile: File, val tempoPercent: Float, val pitchSemitones: Float, val ratePercent: Float = 0f) {
        fun isUnmodified(): Boolean = tempoPercent == 0f && pitchSemitones == 0f && ratePercent == 0f
    }
    
    private data class DerivativeAudio(val audio: BeadsAudio, val quick: Boolean)
    
    private val rawCache: MutableMap<File, BeadsAudio> = ConcurrentHashMap()
    private val originalFileToWav: MutableMap<File, File> = ConcurrentHashMap()
    private val derivativeFileMap: MutableMap<File, MutableList<Derivative>> = ConcurrentHashMap()
    private val derivativeAudioCache: MutableMap<Derivative, DerivativeAudio> = ConcurrentHashMap()
    
    fun clean(keepThese: List<File> = listOf()) {
        (rawCache.keys.toSet() - keepThese.toSet()).forEach { f ->
            rawCache.remove(f)
            originalFileToWav.remove(f)?.delete()
            val deriv = derivativeFileMap[f]
            if (deriv != null) {
                derivativeFileMap.remove(f)
                deriv.forEach { d ->
                    derivativeAudioCache.remove(d)
                }
            }
        }
    }
    
    fun getOrLoad(file: File): BeadsAudio {
        return rawCache.getOrPut(file) { BeadsSoundSystem.newAudio(FileHandle(file)) }
    }
    
    private fun getWavOrLoad(originalFile: File): File {
        val audio = getOrLoad(originalFile)
        val wavFile = originalFileToWav.getOrPut(originalFile) {
            File.createTempFile("lampshade-derivative-", ".wav").apply {
                deleteOnExit()
            }
        }
        if (!wavFile.exists() || wavFile.length() == 0L) {
            // Save the wav version of the original here
            wavFile.createNewFile()
            audio.sample.write(wavFile.canonicalPath)
        }
        return wavFile
    }
    
    private fun createDerivativeAudio(derivative: Derivative, quick: Boolean): DerivativeAudio {
        derivativeFileMap.getOrPut(derivative.originalFile) { mutableListOf() }.also { l ->
            if (derivative !in l) {
                l.add(derivative)
            }
        }
        // If it's not cached, use SoundStretch to create it and cache it
        val originalWav: File = getWavOrLoad(derivative.originalFile)
        val tmpFile: File = File.createTempFile("lampshade-derivative-gen-", ".wav").apply {
            deleteOnExit()
        }
        SoundStretch.processStreams(RHRE3.SOUNDSTRETCH_FOLDER.file(), originalWav, tmpFile,
                                    derivative.tempoPercent, derivative.pitchSemitones, derivative.ratePercent, quick)
        val moddedAudio: BeadsAudio = BeadsSoundSystem.newAudio(FileHandle(tmpFile)).apply {
            tmpFile.delete()
        }
        return DerivativeAudio(moddedAudio, quick)
    }
    
    fun derivativeOf(file: File, tempoPercent: Float, pitchSemitones: Float, ratePercent: Float = 0f, quick: Boolean = false): BeadsAudio {
        return derivativeOf(Derivative(file, tempoPercent, pitchSemitones, ratePercent), quick)
    }
    
    fun derivativeOf(derivative: Derivative, quick: Boolean): BeadsAudio {
        if (derivative.isUnmodified()) {
            return getOrLoad(derivative.originalFile)
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
    
}