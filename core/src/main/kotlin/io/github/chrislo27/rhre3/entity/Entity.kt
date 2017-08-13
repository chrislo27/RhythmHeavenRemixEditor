package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix


abstract class Entity(val remix: Remix) {

    val tmpUpdateBoundsRect = Rectangle()
    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle()
    open var playbackCompletion = PlaybackCompletion.WAITING

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