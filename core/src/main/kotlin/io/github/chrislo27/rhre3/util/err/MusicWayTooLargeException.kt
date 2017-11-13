package io.github.chrislo27.rhre3.util.err

import io.github.chrislo27.toolboks.i18n.Localization


class MusicWayTooLargeException(bytes: Long) : MusicLoadingException(bytes) {

    override fun getLocalizedText(): String {
        return Localization["screen.music.wayTooBig", Int.MAX_VALUE]
    }

}