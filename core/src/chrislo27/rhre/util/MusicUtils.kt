package chrislo27.rhre.util

import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import org.lwjgl.openal.AL10


object MusicUtils {

    fun changePitch(music: OpenALMusic, pitch: Float) {
        AL10.alSourcef(music.sourceId, AL10.AL_PITCH, pitch)
    }

}