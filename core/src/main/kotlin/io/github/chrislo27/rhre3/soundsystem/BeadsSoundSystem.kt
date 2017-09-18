package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.util.FastSeekingMusic

object BeadsSoundSystem : SoundSystem() {
    override fun newSound(handle: FileHandle): Sound {
        TODO()
    }

    override fun newMusic(handle: FileHandle): Music {
        TODO()
    }

    override fun onSet() {
        super.onSet()
        LazySound.soundFactory = BeadsSoundSystem::newSound
        FastSeekingMusic.musicFactory = BeadsSoundSystem::newMusic
    }
}