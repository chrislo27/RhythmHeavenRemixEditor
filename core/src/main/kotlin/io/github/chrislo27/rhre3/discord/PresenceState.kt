package io.github.chrislo27.rhre3.discord

import kotlin.math.roundToLong


sealed class PresenceState(open val state: String = "") {

    open fun getPartyCount(): Pair<Int, Int> = DefaultRichPresence.DEFAULT_PARTY

    open fun modifyRichPresence(richPresence: DefaultRichPresence) {
    }

    // ---------------- IMPLEMENTATIONS BELOW ----------------

    object Loading
        : PresenceState("Loading...")

    object InEditor
        : PresenceState("In Editor")

    object Exporting
        : PresenceState("Exporting a remix")

    object Uploading
        : PresenceState("Uploading a remix")

    object InSettings
        : PresenceState("In Info and Settings")

    object ViewingCredits
        : PresenceState("Viewing the credits ❤")

    class PresentationMode(val duration: Float)
        : PresenceState("In Presentation Mode") {
        override fun modifyRichPresence(richPresence: DefaultRichPresence) {
            super.modifyRichPresence(richPresence)
            if (duration > 0f) {
                richPresence.endTimestamp = System.currentTimeMillis() / 1000L + duration.roundToLong()
            }
        }
    }

}
