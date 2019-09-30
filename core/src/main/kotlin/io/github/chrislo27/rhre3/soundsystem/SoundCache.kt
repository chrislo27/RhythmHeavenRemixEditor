package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.RHRE3
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


/**
 * Singleton handler for sound data and reference counting.
 */
object SoundCache {

    private data class AudioPtrData(val pointer: AudioPointer, var numReferences: Int)

    private val cache: ConcurrentMap<SampleID, AudioPtrData> = ConcurrentHashMap()

    fun isLoaded(id: SampleID): Boolean = cache.containsKey(id)
    fun getNumReferences(id: SampleID): Int = cache[id]?.numReferences ?: 0
    fun getNumLoaded(): Int = cache.size
    fun getTotalReferences(): Int = cache.values.sumBy { it.numReferences }
    
    /**
     * Loads the audio file and increments the internal reference counter.
     * @param doNotTrack If true, this call is not counted towards the number of references.
     */
    fun load(id: SampleID, doNotTrack: Boolean = false): AudioPointer {
        val ptrData = cache[id]
        if (ptrData == null) {
            val initialRefCount = if (doNotTrack) 0 else 1
            // Create and load the audio pointer
            if (id.derivative.isUnmodified()) {
                val newData = AudioPtrData(AudioPointer.BaseAudioPointer(id, BeadsSoundSystem.newAudio(FileHandle(id.file))), initialRefCount)
                cache[id] = newData
                return newData.pointer
            } else {
                // Depends on parent unmodified derivative version of sample
                // This derivative is a dependency on the parent (counts towards # of references)
                val parent: AudioPointer.BaseAudioPointer = load(id.copy(derivative = Derivative.NO_CHANGES)) as AudioPointer.BaseAudioPointer
                // Create the derivative
                val derivative = id.derivative
                val originalWav: File = parent.wavFile
                val tmpFile: File = File.createTempFile("rhre-lampshade-derivative-gen-", ".wav").apply {
                    deleteOnExit()
                }
                SoundStretch.processStreams(RHRE3.SOUNDSTRETCH_FOLDER.file(), originalWav, tmpFile,
                                            derivative.tempoPercent, derivative.pitchSemitones, derivative.ratePercent, false)
                val moddedAudio: BeadsAudio = BeadsSoundSystem.newAudio(FileHandle(tmpFile))
                tmpFile.delete()
                
                val newData = AudioPtrData(AudioPointer.DerivAudioPointer(id, moddedAudio), initialRefCount)
                cache[id] = newData
                return newData.pointer
            }
        } else {
            if (!doNotTrack) {
                ptrData.numReferences++
            }
            return ptrData.pointer
        }
    }

    /**
     * Decrements the internal reference counter for this sample ID.
     * If the ref. counter is <= zero then the entire sample is unloaded.
     */
    fun unload(id: SampleID) {
        val ptrData = cache[id]
        if (ptrData != null) {
            ptrData.numReferences--
            // Unload if no more references are held
            if (ptrData.numReferences <= 0) {
                cache.remove(id, ptrData)
                // If this is a derivative, we have to also handle dereferencing the parent
                if (!id.derivative.isUnmodified()) {
                    unload(id.copy(derivative = Derivative.NO_CHANGES))
                }
            }
        }
    }

    /**
     * Unloads all samples.
     */
    fun unloadAll() {
        cache.clear()
    }
    
    /**
     * Unloads all derivative samples.
     */
    fun unloadAllDerivatives() {
        cache.keys.filter { !it.derivative.isUnmodified() }.forEach {
            cache.remove(it)
            unload(it.copy(derivative = Derivative.NO_CHANGES))
        }
    }

}

/**
 * A simple parameter class for SoundStretch.
 */
data class Derivative(val tempoPercent: Float, val pitchSemitones: Float, val ratePercent: Float = 0f) {

    companion object {
        val NO_CHANGES: Derivative = Derivative(0f, 0f, 0f).apply {
            require(this.isUnmodified())
        }
    }

    fun isUnmodified(): Boolean = tempoPercent == 0f && pitchSemitones == 0f && ratePercent == 0f
}


/**
 * Data class pointing to the sound file and what derivative params it has.
 */
data class SampleID(val file: File, val derivative: Derivative)

/**
 * Contains sample data for a given audio file.
 */
sealed class AudioPointer(val sampleID: SampleID, val audio: BeadsAudio) {

    /**
     * No derivative change.
     */
    class BaseAudioPointer(sampleID: SampleID, audio: BeadsAudio)
        : AudioPointer(sampleID, audio) {
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
    }

    /**
     * Contains the derivative params for this sample.
     */
    class DerivAudioPointer(sampleID: SampleID, audio: BeadsAudio)
        : AudioPointer(sampleID, audio) {
        val derivative: Derivative
            get() = sampleID.derivative
    }
    
}
