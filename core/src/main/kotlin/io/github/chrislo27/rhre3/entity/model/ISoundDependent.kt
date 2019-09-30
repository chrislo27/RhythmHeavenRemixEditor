package io.github.chrislo27.rhre3.entity.model


/**
 * An entity implementing this interface depends on sounds.
 * It should expect to load its sounds at any time, including during playback.
 * Ideally, [preloadSounds] would be called beforehand to avoid stuttering during playback.
 * When deleted or otherwise unloaded, [unloadSounds] will be called.
 */
interface ISoundDependent {
    
    fun preloadSounds()
    
    fun unloadSounds()

}