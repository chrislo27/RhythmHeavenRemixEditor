package io.github.chrislo27.toolboks.util

import org.lwjgl.glfw.GLFW
import kotlin.concurrent.thread

/**
 * A highly accurate sync method that continually adapts to the system
 * it runs on to provide reliable results.
 * http://forum.lwjgl.org/index.php?topic=6582.0
 *
 * @author Riven
 * @author kappaOne
 */
object Sync {
    
    /** number of nano seconds in a second  */
    private const val NANOS_IN_SECOND = 1000L * 1000L * 1000L
    
    /** The time to sleep/yield until the next frame  */
    private var nextFrame: Long = 0

    /** whether the initialisation code has run  */
    private var initialised = false

    /** for calculating the averages the previous sleep/yield times are stored  */
    private val sleepDurations = RunningAvg(10)
    private val yieldDurations = RunningAvg(10)

    /**
     * An accurate sync method that will attempt to run at a constant frame rate.
     * It should be called once every frame.
     *
     * @param fps - the desired frame rate, in frames per second
     */
    fun sync(fps: Int) {
        if (fps <= 0) return
        if (!initialised) initialise()
        try {
            // sleep until the average sleep time is greater than the time remaining till nextFrame
            run {
                var t0 = time
                var t1: Long
                while (nextFrame - t0 > sleepDurations.avg()) {
                    Thread.sleep(1)
                    sleepDurations.add(time.also { t1 = it } - t0) // update average sleep time
                    t0 = t1
                }
            }

            // slowly dampen sleep average if too high to avoid yielding too much
            sleepDurations.dampenForLowResTicker()

            // yield until the average yield time is greater than the time remaining till nextFrame
            var t0 = time
            var t1: Long
            while (nextFrame - t0 > yieldDurations.avg()) {
                Thread.yield()
                yieldDurations.add(time.also { t1 = it } - t0) // update average yield time
                t0 = t1
            }
        } catch (e: InterruptedException) {
        }

        // schedule next frame, drop frame(s) if already too late for next frame
        nextFrame = (nextFrame + NANOS_IN_SECOND / fps).coerceAtLeast(time)
    }

    /**
     * This method will initialise the sync method by setting initial
     * values for sleepDurations/yieldDurations and nextFrame.
     *
     * If running on windows it will start the sleep timer fix.
     */
    private fun initialise() {
        initialised = true
        sleepDurations.init(1000 * 1000.toLong())
        yieldDurations.init((-(time - time) * 1.333).toLong())
        nextFrame = time
        val osName = System.getProperty("os.name")
        if (osName.startsWith("Win")) {
            // On windows the sleep functions can be highly inaccurate by
            // over 10ms making in unusable. However it can be forced to
            // be a bit more accurate by running a separate sleeping daemon thread.
            thread(start = true, isDaemon = true, name = "LWJGL3 Sync Timer") {
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Get the system time in nano seconds
     *
     * @return will return the current time in nano's
     */
    private val time: Long
        get() = (GLFW.glfwGetTime() * NANOS_IN_SECOND).toLong()

    private class RunningAvg(slotCount: Int) {
        private val slots: LongArray
        private var offset: Int
        fun init(value: Long) {
            while (offset < slots.size) {
                slots[offset++] = value
            }
        }

        fun add(value: Long) {
            slots[offset++ % slots.size] = value
            offset %= slots.size
        }

        fun avg(): Long {
            var sum: Long = 0
            for (i in slots.indices) {
                sum += slots[i]
            }
            return sum / slots.size
        }

        fun dampenForLowResTicker() {
            if (avg() > DAMPEN_THRESHOLD) {
                for (i in slots.indices) {
                    slots[i] = (slots[i] * DAMPEN_FACTOR).toLong()
                }
            }
        }

        companion object {
            private const val DAMPEN_THRESHOLD = 10 * 1000L * 1000L // 10ms
            private const val DAMPEN_FACTOR = 0.9f // don't change: 0.9f is exactly right!
        }

        init {
            slots = LongArray(slotCount)
            offset = 0
        }
    }
}