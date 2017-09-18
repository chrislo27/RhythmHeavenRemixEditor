package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.util.FastSeekingMusic


sealed class SoundSystem {

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

    abstract fun newSound(handle: FileHandle): Sound

    abstract fun newMusic(handle: FileHandle): Music

    open fun onSet() {

    }

}

object GdxSoundSystem : SoundSystem() {

    override fun newSound(handle: FileHandle): Sound {
        return Gdx.audio.newSound(handle)
    }

    override fun newMusic(handle: FileHandle): Music {
        return Gdx.audio.newMusic(handle)
    }

}

object BeadsSoundSystem : SoundSystem() {
    override fun newSound(handle: FileHandle): Sound {
        TODO()
    }

    override fun newMusic(handle: FileHandle): Music {
        TODO()
    }

    override fun onSet() {
        super.onSet()
        // TODO
        LazySound.soundFactory
        FastSeekingMusic.musicFactory
    }
}
