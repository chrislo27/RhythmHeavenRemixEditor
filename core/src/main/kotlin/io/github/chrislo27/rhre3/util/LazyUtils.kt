package io.github.chrislo27.rhre3.util

import kotlin.reflect.KProperty


interface SettableLazy<T> {
    /**
     * The value will be initialized on first-get (but NOT first-set).
     */
    var value: T

    fun isInitialized(): Boolean

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        if (!isInitialized()) {
            value
        }
        value = newValue
    }
}

private class SettableLazyImpl<T>(private val initBlock: () -> T) : SettableLazy<T> {

    private var inited: Boolean = false
    private var backing: T? = null

    override var value: T
        get() {
            if (!isInitialized()) {
                init()
            }
            return backing!!
        }
        set(value) {
            backing = value
            inited = true
        }

    private fun init() {
        backing = initBlock()
    }

    override fun isInitialized(): Boolean {
        return inited
    }

}

fun <T> settableLazy(initBlock: () -> T): SettableLazy<T> = SettableLazyImpl(initBlock)