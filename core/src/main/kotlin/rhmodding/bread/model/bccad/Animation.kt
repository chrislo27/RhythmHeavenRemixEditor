package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import rhmodding.bread.model.IAnimation


class Animation : IAnimation {

    override val steps: MutableList<AnimationStep> = mutableListOf()
    var interpolationInt: Int = 0
    var interpolated: Boolean
        get() = (interpolationInt and 0x1) > 0
        set(value) {
            interpolationInt = if (value) (interpolationInt or (1 shl 0x1)) else (interpolationInt and (1 shl 0x1).inv())
        }
    var name: String = ""
    
    override fun copy(): Animation {
        return Animation().also {
            it.interpolationInt = interpolationInt
            steps.mapTo(it.steps) { it.copy() }
        }
    }
    
    override fun toString(): String {
        return "Animation=[interpolation=0x${interpolationInt.toUInt().toString(16)}, numSteps=${steps.size}, steps=[${steps.joinToString(separator = "\n")}]]"
    }

    fun getCurrentStep(frameNum: Int): AnimationStep? {
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