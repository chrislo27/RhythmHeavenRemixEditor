package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Animation(val name: String) {
    var steps: MutableList<AnimationStep> = mutableListOf()

    operator fun get(index: Int): AnimationStep {
        return steps[index]
    }

    fun addNewStep() {
        val step = AnimationStep(0, 1)
        step.unknownData.addAll(listOf(0, 0))
        steps.add(step)
    }

    companion object {
        fun fromBuffer(buf: ByteBuffer): Animation {
            var s = ""
            val n = buf.get()
            repeat(n.toInt()) {
                s += buf.get().toChar()
            }
            repeat(4 - ((n + 1) % 4)) {
                buf.get()
            }
            val a = Animation(s)
            buf.int
            repeat(buf.int) {
                a.steps.add(AnimationStep.fromBuffer(buf))
            }
            return a
        }
    }

    fun getCurrentStep(frameNum: Int): AnimationStep? {
        val totalFrames = steps.sumBy { it.duration.toInt() }
        val frame = frameNum % totalFrames

        var currentFrame = 0
        return steps.firstOrNull {
            val result = frame in currentFrame..(currentFrame + it.duration)
            currentFrame += it.duration
            result
        }
    }

    fun render(batch: SpriteBatch, sheet: Texture, sprites: List<Sprite>, frameNum: Int, offsetX: Float, offsetY: Float): AnimationStep? {
        val step =  getCurrentStep(frameNum)
        step?.render(batch, sheet, sprites, offsetX, offsetY)
        return step
    }

    fun toBytes(): List<Byte> {
        val l = mutableListOf<Byte>()
        l.add(name.length.toByte())
        l.addAll(name.toCharArray().map { it.toByte() })
        l.addAll(ByteArray(4 - ((name.length + 1) % 4)).toList())
        val a = ByteArray(8)
        val bb = ByteBuffer.wrap(a).order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(4, steps.size)
        l.addAll(a.toList())
        for (s in steps) {
            l.addAll(s.toBytes())
        }
        return l
    }

    override fun toString(): String {
        return "Animation $name: {\n" + steps.map { "\t\t" + it.toString() }.joinToString("\n") + "\n\t}"
    }
}