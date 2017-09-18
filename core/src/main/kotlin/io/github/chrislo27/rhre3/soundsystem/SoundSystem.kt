package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle


sealed class SoundSystem {

    companion object {

        lateinit var system: SoundSystem
            private set

        fun setSoundSystem(system: SoundSystem) {
            try {
                SoundSystem.system
                error("Sound system already set to ${system::class.simpleName}")
            } catch (e: UninitializedPropertyAccessException) {
                SoundSystem.system = system
            }
        }

    }

    abstract fun newSound(handle: FileHandle): Sound

    abstract fun newMusic(handle: FileHandle): Music

}

object GdxSoundSystem : SoundSystem() {

    override fun newSound(handle: FileHandle): Sound {
        return Gdx.audio.newSound(handle)
    }

    override fun newMusic(handle: FileHandle): Music {
        return Gdx.audio.newMusic(handle)
    }

}
