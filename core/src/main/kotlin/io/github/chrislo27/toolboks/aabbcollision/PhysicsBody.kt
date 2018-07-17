package io.github.chrislo27.toolboks.aabbcollision

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool


/**
 * A simple object with a bounds rectangle and a velocity.
 */
class PhysicsBody() : Pool.Poolable {

    val bounds = Rectangle(0f, 0f, 1f, 1f)
    val velocity = Vector2()
    var metadata: Any = Unit

    constructor(x: Float, y: Float, width: Float, height: Float, vx: Float, vy: Float) : this() {
        bounds.set(x, y, width, height)
        velocity.set(vx, vy)
    }

    override fun reset() {
        bounds.set(0f, 0f, 1f, 1f)
        velocity.set(0f, 0f)
        metadata = Unit
    }
}