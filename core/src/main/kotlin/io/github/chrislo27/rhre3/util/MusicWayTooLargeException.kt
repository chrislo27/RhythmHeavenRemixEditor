package io.github.chrislo27.rhre3.util

import io.github.chrislo27.toolboks.i18n.Localization


class MusicWayTooLargeException(val bytes: Long) : RuntimeException() {

    fun getLocalizedText(): String {
        return Localization["screen.music.wayTooBig", Int.MAX_VALUE]
    }

}