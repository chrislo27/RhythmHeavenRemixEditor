package io.github.chrislo27.rhre3.soundsystem

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.util.FastSeekingMusic

object GdxSoundSystem : SoundSystem() {

    override fun newSound(handle: FileHandle): Sound {
        return Gdx.audio.newSound(handle)
    }

    override fun newMusic(handle: FileHandle): Music {
        return Gdx.audio.newMusic(handle)
    }

    override fun onSet() {
        super.onSet()
        LazySound.soundFactory = GdxSoundSystem::newSound
        FastSeekingMusic.musicFactory = GdxSoundSystem::newMusic
    }
}