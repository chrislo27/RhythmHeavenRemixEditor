package io.github.chrislo27.rhre3.discord

import club.minnced.discord.rpc.DiscordRichPresence
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application


class DefaultRichPresence(state: String = "",
                          party: Pair<Int, Int> = DEFAULT_PARTY,
                          smallIcon: String = "",
                          smallIconText: String = state,
                          largeIcon: String? = null,
                          largeIconText: String? = null)
    : DiscordRichPresence() {

    companion object {
        val DEFAULT_PARTY: Pair<Int, Int> = 0 to 0
    }

    constructor(presenceState: PresenceState)
            : this(presenceState.state, presenceState.getPartyCount(), presenceState.smallIcon, presenceState.smallIconText,
                   presenceState.largeIcon, presenceState.largeIconText) {
        presenceState.modifyRichPresence(this)
    }

    init {
        details = if (RHRE3.VERSION.suffix.startsWith("DEV")) {
            "Working on ${RHRE3.VERSION.copy(suffix = "")}"
        } else if (RHRE3.VERSION.suffix.startsWith("RC") || RHRE3.VERSION.suffix.startsWith("SNAPSHOT")) {
            "Testing ${RHRE3.VERSION}"
        } else {
            "Using ${RHRE3.VERSION}"
        }
        startTimestamp = RHRE3Application.instance.startTimeMillis / 1000L // Epoch seconds
        largeImageKey = largeIcon ?: DiscordHelper.DEFAULT_LARGE_IMAGE
        largeImageText = largeIconText ?: "RHRE is a custom remix editor for the Rhythm Heaven series"
        smallImageKey = smallIcon
        smallImageText = smallIconText
        this.state = state
        if (party.first > 0 && party.second > 0) {
            partySize = party.first
            partyMax = party.second
        }
    }

}