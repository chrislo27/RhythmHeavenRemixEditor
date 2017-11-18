package io.github.chrislo27.rhre3.soundsystem.gdx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.soundsystem.Music
import io.github.chrislo27.rhre3.soundsystem.Sound
import io.github.chrislo27.rhre3.soundsystem.SoundSystem

object GdxSoundSystem : SoundSystem() {

    override val id: String = "gdx"
    val soundList: MutableList<GdxSoundWrapper> = mutableListOf()

    override fun resume() {
        soundList.forEach {
            it.original.resume()
        }
    }

    override fun pause() {
        soundList.forEach {
            it.original.pause()
        }
    }

    override fun stop() {
        soundList.forEach {
            it.original.stop()
        }
    }

    override fun newSound(handle: FileHandle): Sound {
        return GdxSoundWrapper(Gdx.audio.newSound(handle)).apply {
            soundList += this
        }
    }

    override fun newMusic(handle: FileHandle): Music {
        return GdxMusicWrapper(handle)
    }

}

class GdxSoundWrapper(val original: com.badlogic.gdx.audio.Sound) : Sound {

    override fun play(loop: Boolean, pitch: Float, rate: Float, volume: Float): Long {
        val id = original.play(volume, pitch * rate, 0f)
        original.setLooping(id, loop)
        return id
    }

    override fun setPitch(id: Long, pitch: Float) {
        original.setPitch(id, pitch)
    }

    override fun setRate(id: Long, rate: Float) {
        error("Not implemented for the GDX sound system")
    }

    override fun setVolume(id: Long, vol: Float) {
        original.setVolume(id, vol)
    }

    override fun stop(id: Long) {
        original.stop(id)
    }

    override fun dispose() {
        GdxSoundSystem.soundList -= this
        original.dispose()
    }
}

class GdxMusicWrapper(handle: FileHandle) : Music {

    val music: com.badlogic.gdx.audio.Music = Gdx.audio.newMusic(handle)

    override fun update(delta: Float) {
//        fastSeeking.update(delta)
    }

    override fun isPlaying(): Boolean {
        return music.isPlaying
    }

    override fun play() {
        music.play()
    }

    override fun pause() {
        music.pause()
    }

    override fun stop() {
        music.stop()
    }

    override fun getPosition(): Float {
        return music.position
    }

    override fun setPosition(seconds: Float) {
        music.position = seconds
    }

    override fun getVolume(): Float {
        return music.volume
    }

    override fun setVolume(vol: Float) {
        music.volume = vol
    }

    override fun dispose() {
        music.dispose()
    }
}
