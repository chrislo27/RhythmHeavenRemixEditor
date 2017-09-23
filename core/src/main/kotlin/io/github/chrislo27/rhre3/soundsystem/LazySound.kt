package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle


class LazySound(val handle: FileHandle) {

    @Volatile
    var isLoaded = false
    private var backingSound: Sound? = null

    val sound: Sound
        get() {
            if (!isLoaded) {
                load()
            }
            return backingSound!!
        }

    fun load() {
        if (isLoaded)
            return

        unload()

        backingSound = SoundSystem.system.newSound(handle)
        isLoaded = true
    }

    fun unload() {
        if (!isLoaded || backingSound == null)
            return

        backingSound?.dispose()
        backingSound = null
        isLoaded = false
    }

}