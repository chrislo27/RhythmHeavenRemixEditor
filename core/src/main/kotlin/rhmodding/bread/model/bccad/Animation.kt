package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import rhmodding.bread.model.IAnimation


class Animation : IAnimation {

    override val steps: MutableList<AnimationStep> = mutableListOf()
    var interpolationInt: Int = 0
    var interpolated: Boolean
        get() = (interpolationInt and 0b1) > 0
        set(value) {
            interpolationInt = if (value) (interpolationInt or 0b1) else (interpolationInt and 0b1.inv())
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

    fun renderWithShader(batch: SpriteBatch, shader: ShaderProgram, sheet: Texture, sprites: List<Sprite>, frameNum: Int, offsetX: Float, offsetY: Float): AnimationStep? {
        val step =  getCurrentStep(frameNum)
        step?.renderWithShader(batch, shader, sheet, sprites, offsetX, offsetY)
        return step
    }
    
}