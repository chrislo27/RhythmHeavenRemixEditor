package io.github.chrislo27.toolboks.tick

/**
 * @see TickController
 */
@FunctionalInterface
interface TickHandler {

    fun tickUpdate(tickController: TickController)

}