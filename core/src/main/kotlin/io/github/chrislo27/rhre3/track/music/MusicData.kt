package io.github.chrislo27.rhre3.track.music

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.GranularMusic


class MusicData(val handle: FileHandle, val remix: Remix) {

    val music: GranularMusic = GranularMusic(handle)

}