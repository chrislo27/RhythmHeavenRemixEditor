package io.github.chrislo27.rhre3.util

import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.MemoryUtils


class MusicTooLargeException(val bytes: Long, val original: OutOfMemoryError) : RuntimeException() {

    fun getLocalizedText(): String {
        return Localization["screen.music.tooBig", RHRE3.OUT_OF_MEMORY_DOC_LINK,
                bytes / (1024 * 1024),
                MemoryUtils.maxMemory / 1024]
    }

}