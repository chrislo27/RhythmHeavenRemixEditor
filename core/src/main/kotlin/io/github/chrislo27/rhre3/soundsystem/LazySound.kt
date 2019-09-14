package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle


class LazySound(val handle: FileHandle) {

    @Volatile
    var isLoaded = false
    private var backingSound: BeadsSound? = null

    val beadsSound: BeadsSound
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

        backingSound = BeadsSoundSystem.newSound(handle)
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