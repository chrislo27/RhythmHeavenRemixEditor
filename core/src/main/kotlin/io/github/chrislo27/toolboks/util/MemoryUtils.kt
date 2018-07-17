package io.github.chrislo27.toolboks.util

/**
 * Some properties you can use to get the current memory stats *in kilobytes*.
 */
object MemoryUtils {

    val usedMemory: Int
        get() = (Runtime.getRuntime().totalMemory() / 1024).toInt()

    val maxMemory: Int
        get() = (Runtime.getRuntime().maxMemory() / 1024).toInt()

    val freeMemory: Int
        get() = (Runtime.getRuntime().freeMemory() / 1024).toInt()

}