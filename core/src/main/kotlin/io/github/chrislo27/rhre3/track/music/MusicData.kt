package io.github.chrislo27.rhre3.track.music

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.FastSeekingMusic
import java.io.InputStream


class MusicData(val handle: FileHandle, val remix: Remix)
    : Disposable {

    val music: FastSeekingMusic = FastSeekingMusic(handle)
    private val reader: InputStream = handle.read()

    override fun dispose() {
        music.dispose()
        StreamUtils.closeQuietly(reader)
    }
}