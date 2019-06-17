package io.github.chrislo27.rhre3.sfxdb

import com.badlogic.gdx.files.FileHandle


/**
 * Represents an [FileHandle] with more metadata regarding a SFX folder.
 * The [dataJson] file handle nor the [textureFh] file handle may not exist in the file system.
 */
data class SFXDirectory(val folder: FileHandle, val isCustom: Boolean, val dataJson: FileHandle) {
    val textureFh: FileHandle = folder.child(SFXDatabase.ICON_FILENAME)
}