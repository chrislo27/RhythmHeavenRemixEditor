package io.github.chrislo27.rhre3.discord

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import kotlin.concurrent.thread


object DiscordHelper {

    const val DISCORD_APP_ID = "278329593012682754"
    const val DEFAULT_LARGE_IMAGE = "square_logo"
    private var inited = false

    private val lib: DiscordRPC
        get() = DiscordRPC.INSTANCE

    @Synchronized
    fun init() {
        if (inited)
            return
        inited = true

        lib.Discord_Initialize(DISCORD_APP_ID, DiscordEventHandlers(), true, "")


        Runtime.getRuntime().addShutdownHook(
                thread(start = false, name = "Discord-RPC Shutdown", block = lib::Discord_Shutdown))

        thread(isDaemon = true, name = "Discord-RPC Callback Handler") {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(2000L)
                } catch (ignored: InterruptedException) {
                }
                lib.Discord_RunCallbacks()
            }
        }
    }

    @Synchronized
    fun clearPresence() {
        lib.Discord_ClearPresence()
    }

    @Synchronized
    fun updatePresenceDefault(presence: DiscordRichPresence) {
        lib.Discord_UpdatePresence(presence)
    }

    @Synchronized
    inline fun updatePresenceDefault(presenceFunc: DefaultRichPresence.() -> Unit) {
        updatePresenceDefault(DefaultRichPresence().apply {
            presenceFunc()
        })
    }

}
