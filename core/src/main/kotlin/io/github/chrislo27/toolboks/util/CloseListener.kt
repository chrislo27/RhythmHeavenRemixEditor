package io.github.chrislo27.toolboks.util


interface CloseListener {

    /**
     * @return True if we can close the application
     */
    fun attemptClose(): Boolean

}