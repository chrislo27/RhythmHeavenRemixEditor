package io.github.chrislo27.toolboks.aabbcollision

/**
 * Represents a side in a coordinate plane where the origin is the bottom-left.
 */
enum class Normal(val x: Int, val y: Int) {

    NONE(0, 0), TOP(0, 1), BOTTOM(0, -1), LEFT(-1, 0), RIGHT(1, 0)

}