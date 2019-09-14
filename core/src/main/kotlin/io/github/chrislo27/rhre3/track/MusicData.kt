package io.github.chrislo27.rhre3.track

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.rhre3.soundsystem.BeadsMusic
import io.github.chrislo27.rhre3.soundsystem.BeadsSoundSystem
import java.io.InputStream


class MusicData(val handle: FileHandle, val remix: Remix)
    : Disposable {

    val music: BeadsMusic = BeadsSoundSystem.newMusic(handle)
    private val reader: InputStream = handle.read()

    override fun dispose() {
        music.dispose()
        StreamUtils.closeQuietly(reader)
    }
}