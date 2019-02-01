package io.github.chrislo27.rhre3.entity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.RectanglePool

fun List<Entity>.areAnyResponseCopyable(): Boolean {
    return this.all { it is ModelEntity<*> && it.datamodel is ResponseModel } &&
            this.any { it is ModelEntity<*> && it.datamodel is ResponseModel && it.datamodel.responseIDs.isNotEmpty() }
}

abstract class Entity(val remix: Remix) {

    companion object {
        fun getEntityFromType(type: String, node: ObjectNode, remix: Remix): Entity? {
            return when (type) {
                "model" -> {
                    val datamodelID = node[ModelEntity.JSON_DATAMODEL].asText(null)
                            ?: error("Entity of type 'model' is missing field ${ModelEntity.JSON_DATAMODEL}")

                    GameRegistry.data.objectMap[datamodelID]?.createEntity(remix, null)
                }
                else -> error("Unsupported entity type: $type")
            }
        }
    }

    var isSelected: Boolean = false
    val bounds: Rectangle = Rectangle()
    val lerpDifference: Rectangle = Rectangle()
    open val supportsCopying: Boolean = true
    open var playbackCompletion = PlaybackCompletion.WAITING

    abstract val jsonType: String

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

    open fun inRenderRange(start: Float, end: Float): Boolean {
        return bounds.x + lerpDifference.x + bounds.width + lerpDifference.width >= start
                && bounds.x + lerpDifference.x <= end
    }

    open fun isUpdateable(beat: Float): Boolean {
        return beat in getLowerUpdateableBound()..getUpperUpdateableBound()
    }

    open fun getLowerUpdateableBound(): Float {
        return bounds.x
    }

    open fun getUpperUpdateableBound(): Float {
        return bounds.x + bounds.width
    }

    open fun onBoundsChange(old: Rectangle) {
        lerpDifference.x = (old.x + lerpDifference.x) - bounds.x
        lerpDifference.y = (old.y + lerpDifference.y) - bounds.y
        lerpDifference.width = (old.width + lerpDifference.width) - bounds.width
        lerpDifference.height = (old.height + lerpDifference.height) - bounds.height
    }

    open fun isFinished(): Boolean =
            remix.beat > bounds.x + bounds.width

    /**
     * Automatically calls onBoundsChange and caches the old rectangle.
     */
    inline fun updateBounds(func: () -> Unit) {
        RectanglePool.use { rect ->
            rect.set(bounds)
            func()
            onBoundsChange(rect)
        }
    }

    open fun saveData(objectNode: ObjectNode) {
        objectNode.put("beat", bounds.x)
                .put("track", bounds.y.toInt())
                .put("width", bounds.width)
                .put("height", bounds.height.toInt())

        if (this is IRepitchable && this.persistSemitoneData) {
            objectNode.put("semitone", this.semitone)
        }
        if (this is IVolumetric && this.persistVolumeData) {
            objectNode.put("volume", this.volumePercent)
        }
    }

    open fun readData(objectNode: ObjectNode) {
        updateBounds {
            bounds.set(
                    objectNode["beat"].floatValue(),
                    objectNode["track"].floatValue().toInt().toFloat(),
                    objectNode["width"].floatValue(),
                    objectNode["height"].floatValue().toInt().toFloat()
                      )
        }

        if (this is IRepitchable) {
            semitone = objectNode["semitone"]?.asInt(0) ?: 0
        }
        if (this is IVolumetric) {
            volumePercent = objectNode["volume"]?.asInt(IVolumetric.DEFAULT_VOLUME) ?: IVolumetric.DEFAULT_VOLUME
        }
    }

    open fun updateInterpolation(forceUpdate: Boolean) {
        if (forceUpdate) {
            lerpDifference.x = 0f
            lerpDifference.y = 0f
            lerpDifference.width = 0f
            lerpDifference.height = 0f

            return
        }

        val delta: Float = Gdx.graphics.deltaTime
        val speedX: Float = 32f
        val speedY: Float = speedX
        val alphaX: Float = (delta * speedX).coerceAtMost(1f)
        val alphaY: Float = (delta * speedY).coerceAtMost(1f)

        lerpDifference.x = MathUtils.lerp(lerpDifference.x, 0f, alphaX)
        lerpDifference.y = MathUtils.lerp(lerpDifference.y, 0f, alphaY)
        lerpDifference.width = MathUtils.lerp(lerpDifference.width, 0f, alphaX)
        lerpDifference.height = MathUtils.lerp(lerpDifference.height, 0f, alphaY)
    }

    abstract fun render(batch: SpriteBatch)

    abstract fun onStart()

    abstract fun whilePlaying()

    abstract fun onEnd()

    abstract fun copy(remix: Remix = this.remix): Entity

}