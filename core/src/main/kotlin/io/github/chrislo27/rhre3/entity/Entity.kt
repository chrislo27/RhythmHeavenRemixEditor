package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix


abstract class Entity(val remix: Remix) {

    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle()
    open var playbackCompletion = PlaybackCompletion.WAITING

    open fun onBoundsChange(old: Rectangle) {

    }

    open fun isFinished(): Boolean =
            remix.beat > bounds.x + bounds.width

    abstract fun render(batch: SpriteBatch)

    abstract fun onStart()

    abstract fun whilePlaying()

    abstract fun onEnd()

    abstract fun copy(remix: Remix = this.remix): Entity

}