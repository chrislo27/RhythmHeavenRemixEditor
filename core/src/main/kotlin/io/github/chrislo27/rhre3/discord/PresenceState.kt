package io.github.chrislo27.rhre3.discord

import kotlin.math.roundToLong


sealed class PresenceState {

    abstract fun getState(): String

    open fun getPartyCount(): Pair<Int, Int> = DefaultRichPresence.DEFAULT_PARTY

    open fun modifyRichPresence(richPresence: DefaultRichPresence) {
    }

    open class SimplePresenceState(val stateStr: String) : PresenceState() {
        final override fun getState(): String = stateStr
    }

    object Loading : SimplePresenceState("Loading...")

    object InEditor : SimplePresenceState("In Editor")

    object Exporting : SimplePresenceState("Exporting a remix")

    object Uploading : SimplePresenceState("Uploading a remix")

    object InSettings : SimplePresenceState("In Info and Settings")

    class PresentationMode(val duration: Float) : PresenceState() {
        override fun getState(): String {
            return "In Presentation Mode"
        }

        override fun modifyRichPresence(richPresence: DefaultRichPresence) {
            super.modifyRichPresence(richPresence)
            if (duration > 0f) {
                richPresence.endTimestamp = System.currentTimeMillis() / 1000L + duration.roundToLong()
            }
        }
    }

}
