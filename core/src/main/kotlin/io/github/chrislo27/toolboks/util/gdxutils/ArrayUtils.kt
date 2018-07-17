package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.utils.Array

fun <T> Array<T>.toList(): List<T> =
        this.map { it }

fun <T> Array<T>.toMutableList(): MutableList<T> =
        this.map { it }.toMutableList()

