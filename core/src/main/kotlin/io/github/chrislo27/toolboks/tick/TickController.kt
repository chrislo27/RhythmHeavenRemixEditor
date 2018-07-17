package io.github.chrislo27.toolboks.tick

import io.github.chrislo27.toolboks.ToolboksGame


/**
 * Controls and dispatches tick updates.
 */
class TickController {

    var tickNumber: Long = 0
        private set

    private var inited = false
    private var lastNano: Long = System.nanoTime()
    private val tickHandlers: List<TickHandler> = mutableListOf()

    lateinit var game: ToolboksGame
        private set

    var nanoPerTick: Long = -1L

    var msPerTick: Long
        get() {
            return nanoPerTick / 1_000_000
        }
        set(value) {
            nanoPerTick = (value * 1_000_000)
        }
    var secondsPerTick: Float
        get() {
            return nanoPerTick / 1_000_000_000f
        }
        set(value) {
            nanoPerTick = (value * 1_000_000_000).toLong()
        }

    fun disable(): TickController {
        nanoPerTick = -1
        return this
    }

    fun setTicksPerSecond(ticks: Int): TickController {
        secondsPerTick = 1f / ticks
        return this
    }

    fun init(game: ToolboksGame) {
        if (inited)
            error("Already initialized")

        this.game = game
        this.lastNano = System.nanoTime()

        inited = true
    }

    fun registerHandler(tickHandler: TickHandler): TickController {
        (tickHandlers as MutableList).add(tickHandler)
        return this
    }

    fun registerHandlers(vararg tickHandlers: TickHandler): TickController {
        (this.tickHandlers as MutableList).addAll(tickHandlers)
        return this
    }

    fun unregisterHandler(tickHandler: TickHandler): TickController {
        (tickHandlers as MutableList).remove(tickHandler)
        return this
    }

    fun update() {
        if (!inited || nanoPerTick < 1)
            return

        val nanoDiff: Long = System.nanoTime() - lastNano
        if (nanoDiff >= nanoPerTick) {
            val ticksToExecute: Long = nanoDiff / lastNano

            for (i in 1..ticksToExecute) {
                tickHandlers.forEach {
                    it.tickUpdate(this)
                }
            }
        }

        lastNano = System.nanoTime()
    }

}