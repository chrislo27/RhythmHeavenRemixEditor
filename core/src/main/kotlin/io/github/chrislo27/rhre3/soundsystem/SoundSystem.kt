package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle


abstract class SoundSystem {

    companion object {

        var errorOnDuplicateSystemSet = true

        lateinit var system: SoundSystem
            private set

        fun setSoundSystem(system: SoundSystem) {
            try {
                SoundSystem.system

                if (errorOnDuplicateSystemSet)
                    error("Sound system already set to ${system::class.simpleName}")
            } catch (e: UninitializedPropertyAccessException) {
                SoundSystem.system = system
                system.onSet()
            }
        }

    }

    abstract fun resume()

    abstract fun pause()

    abstract fun stop()

    abstract fun newSound(handle: FileHandle): Sound

    abstract fun newMusic(handle: FileHandle): Music

    open fun onSet() {

    }

}
