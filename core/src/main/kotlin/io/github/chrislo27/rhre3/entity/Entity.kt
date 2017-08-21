package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix


abstract class Entity(val remix: Remix) {

    val tmpUpdateBoundsRect = Rectangle()
    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle()
    open val supportsCopying: Boolean = true
    open var playbackCompletion = PlaybackCompletion.WAITING

    protected fun SpriteBatch.setColorWithTintIfNecessary(selectionTint: Color, r: Float, g: Float, b: Float, a: Float,
                                                          necessary: Boolean = isSelected) {
        if (necessary) {
            this.setColor((r * (1 + selectionTint.r)).coerceIn(0f, 1f),
                          (g * (1 + selectionTint.g)).coerceIn(0f, 1f),
                          (b * (1 + selectionTint.b)).coerceIn(0f, 1f),
                          a)
        } else {
            this.setColor(r, g, b, a)
        }
    }

    protected fun SpriteBatch.setColorWithTintIfNecessary(selectionTint: Color, color: Color,
                                                          necessary: Boolean = isSelected) {
        this.setColorWithTintIfNecessary(selectionTint, color.r, color.g, color.b, color.a, necessary)
    }

    open fun onBoundsChange(old: Rectangle) {

    }

    open fun isFinished(): Boolean =
            remix.beat > bounds.x + bounds.width

    /**
     * Automatically calls onBoundsChange and caches the old rectangle.
     */
    inline fun updateBounds(func: Entity.() -> Unit) {
        val old = tmpUpdateBoundsRect.set(bounds)
        this.func()
        onBoundsChange(old)
    }

    abstract fun render(batch: SpriteBatch)

    abstract fun onStart()

    abstract fun whilePlaying()

    abstract fun onEnd()

    abstract fun copy(remix: Remix = this.remix): Entity

}