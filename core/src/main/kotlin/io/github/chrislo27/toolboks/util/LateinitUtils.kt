package io.github.chrislo27.toolboks.util

import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

fun Any.anyUninitializedLateinits(): Boolean {
    return this.getUninitializedLateinits().isNotEmpty()
}

fun Any.getUninitializedLateinits(): List<KProperty<*>> {
    return this::class.memberProperties
            .filter { it.isLateinit }
            .onEach { it.isAccessible = true }
            .filter { it.javaField?.get(this) == null }
}
