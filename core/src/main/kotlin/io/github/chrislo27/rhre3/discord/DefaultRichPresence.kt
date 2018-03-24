package io.github.chrislo27.rhre3.discord

import club.minnced.discord.rpc.DiscordRichPresence
import io.github.chrislo27.rhre3.RHRE3Application


class DefaultRichPresence : DiscordRichPresence() {

    init {
        startTimestamp = RHRE3Application.instance.startTimeMillis / 1000L // Epoch seconds
        largeImageKey = DiscordHelper.DEFAULT_LARGE_IMAGE
    }

}