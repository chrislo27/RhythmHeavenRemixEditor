package io.github.chrislo27.rhre3.util.err


abstract class MusicLoadingException(val bytes: Long) : RuntimeException() {

    abstract fun getLocalizedText(): String

}