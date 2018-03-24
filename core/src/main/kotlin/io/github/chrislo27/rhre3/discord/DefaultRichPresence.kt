package io.github.chrislo27.rhre3.discord

import club.minnced.discord.rpc.DiscordRichPresence
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application


class DefaultRichPresence : DiscordRichPresence() {

    init {
        details = if (RHRE3.VERSION.suffix == "DEVELOPMENT") "Developing ${RHRE3.VERSION.copy(suffix = "")}" else RHRE3.VERSION.toString()
        startTimestamp = RHRE3Application.instance.startTimeMillis / 1000L // Epoch seconds
        largeImageKey = DiscordHelper.DEFAULT_LARGE_IMAGE
        largeImageText = RHRE3.GITHUB
    }

}