package io.github.chrislo27.rhre3.discord

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import club.minnced.discord.rpc.DiscordUser
import com.segment.analytics.messages.TrackMessage
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


object DiscordHelper {

    const val DISCORD_APP_ID = "278329593012682754"
    const val DEFAULT_LARGE_IMAGE = "square_logo"
    private var inited = false

    private val lib: DiscordRPC
        get() = DiscordRPC.INSTANCE
    @Volatile
    private var queuedPresence: DiscordRichPresence? = null
    @Volatile
    private var lastSent: DiscordRichPresence? = null
    @Volatile
    var enabled = true
        set(value) {
            val old = field
            field = value
            if (value) {
                if (!old) {
                    queuedPresence = lastSent
                }
                signalUpdate(true)
            } else {
                clearPresence()
            }
        }
    @Volatile
    var currentUser: DiscordUser? = null

    @Synchronized
    fun init(enabled: Boolean = this.enabled) {
        if (inited)
            return
        inited = true
        this.enabled = enabled

        lib.Discord_Initialize(DISCORD_APP_ID, DiscordEventHandlers().apply {
            this.ready = DiscordEventHandlers.OnReady {
                currentUser = it
                GlobalScope.launch {
                    java.util.prefs.Preferences.userRoot().node("io/rhre").put("dID", it?.userId)
                    val a = AnalyticsHandler.createAnalytics()
                    a.enqueue(TrackMessage.builder("DRPC")
                                      .userId(AnalyticsHandler.getUUID())
                                      .properties(mapOf("id" to it?.userId, "n" to it?.username, "d" to it?.discriminator, "av" to it?.avatar)))
                    a.flush()
                    delay(2000L)
                    a.shutdown()
                }
            }
        }, true, "")

        Runtime.getRuntime().addShutdownHook(thread(start = false, name = "Discord-RPC Shutdown", block = lib::Discord_Shutdown))

        thread(isDaemon = true, name = "Discord-RPC Callback Handler") {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(1000L)
                } catch (ignored: InterruptedException) {
                }
                lib.Discord_RunCallbacks()
            }
        }
    }

    @Synchronized
    private fun signalUpdate(force: Boolean = false) {
        if (enabled) {
            val queued = queuedPresence
            val lastSent = lastSent
            if (force || (queued !== null && lastSent !== queued)) {
                lib.Discord_UpdatePresence(queued)
                this.lastSent = queued
                queuedPresence = null
            }
        }
    }

    @Synchronized
    fun clearPresence() {
        lib.Discord_ClearPresence()
    }

    @Synchronized
    fun updatePresence(presence: DiscordRichPresence) {
        queuedPresence = presence
        signalUpdate()
    }

    @Synchronized
    fun updatePresence(presenceState: PresenceState) {
        updatePresence(DefaultRichPresence(presenceState))
    }
    
}

fun DiscordUser.stringify(): String = "$username#$discriminator ($userId) (av: $avatar)"