package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.soundsystem.gdx.GdxSoundSystem


abstract class SoundSystem {

    companion object {

        val allSystems: List<SoundSystem> = listOf(BeadsSoundSystem, GdxSoundSystem)
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

    abstract val id: String

    abstract fun resume()

    abstract fun pause()

    abstract fun stop()

    abstract fun newSound(handle: FileHandle): Sound

    abstract fun newMusic(handle: FileHandle): Music

    open fun onSet() {

    }

}
