package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.math.Rectangle


val Rectangle.maxX: Float get() = this.x + this.width
val Rectangle.maxY: Float get() = this.y + this.height

fun Rectangle.overlapsEpsilon(r: Rectangle, epsilon: Float = 0.0001f): Boolean {
    return x + epsilon < r.x + r.width && x + width - epsilon > r.x && y + epsilon < r.y + r.height && y + height - epsilon > r.y
}

/**
 * Same as [Rectangle.overlaps] but has equality instead of just less/greater than comparisons.
 */
fun Rectangle.intersects(r: Rectangle, epsilon: Float = 0.0001f): Boolean {
    return this.overlapsEpsilon(r, epsilon) || (x == r.x && y == r.y && maxX == r.maxX && maxY == r.maxY)
}
