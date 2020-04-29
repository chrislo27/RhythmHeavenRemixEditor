package rhmodding.bread.model.brcad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import rhmodding.bread.model.IAnimation
import rhmodding.bread.util.Unknown


class Animation : IAnimation {

    @Unknown
    var unknown: Short = 0
    override val steps: MutableList<AnimationStep> = mutableListOf()
    
    override fun copy(): Animation {
        return Animation().also {
            it.unknown = unknown
            steps.mapTo(it.steps) { it.copy() }
        }
    }
    
    override fun toString(): String {
        return "Animation=[numSteps=${steps.size}, steps=[${steps.joinToString(separator = "\n")}]]"
    }

    fun getCurrentStep(frameNum: Int): AnimationStep? {
        if (steps.isEmpty()) return null
        if (steps.size == 1) return steps.first()
        val totalFrames = steps.sumBy { it.delay.toInt() }
        val frame = frameNum % totalFrames

        var currentFrame = 0
        return steps.firstOrNull {
            val result = frame in currentFrame..(currentFrame + it.delay.toInt())
            currentFrame += it.delay.toInt()
            result
        }
    }

    fun render(batch: SpriteBatch, sheet: Texture, sprites: List<Sprite>, frameNum: Int, offsetX: Float, offsetY: Float): AnimationStep? {
        val step =  getCurrentStep(frameNum)
        step?.render(batch, sheet, sprites, offsetX, offsetY)
        return step
    }

}