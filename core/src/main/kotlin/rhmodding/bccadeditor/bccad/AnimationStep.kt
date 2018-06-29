package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.transform.Affine
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AnimationStep(var spriteNum: Short, var duration: Short) {

    var tlX: Short = 0
    var tlY: Short = 0
    var opacity: Short = 255
    var depth: Float = 0f
    var rotation: Float = 0f
    var stretchX: Float = 1f
    var stretchY: Float = 1f
    var color: Color = Color.WHITE
    val unknownData = mutableListOf<Byte>()

    companion object {
        fun fromBuffer(buf: ByteBuffer): AnimationStep {
            val a = AnimationStep(buf.short, buf.short)
            a.tlX = buf.short
            a.tlY = buf.short
            a.depth = buf.float
            a.stretchX = buf.float
            a.stretchY = buf.float
            a.rotation = buf.float
            a.color = Color.rgb(buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF)
            buf.get()
            repeat(2) {
                a.unknownData.add(buf.get())
            }
            a.opacity = buf.short
            return a
        }
    }

    fun toBytes(): List<Byte> {
        val firstBytes = ByteArray(28)
        val b = ByteBuffer.wrap(firstBytes).order(ByteOrder.LITTLE_ENDIAN)
        b.putShort(spriteNum)
        b.putShort(duration)
        b.putShort(tlX)
        b.putShort(tlY)
        b.putFloat(depth)
        b.putFloat(stretchX)
        b.putFloat(stretchY)
        b.putFloat(rotation)
        b.put((color.red * 255).toByte())
        b.put((color.green * 255).toByte())
        b.put((color.blue * 255).toByte())
        b.put(0.toByte())
        val l = firstBytes.toMutableList()
        l.addAll(unknownData)
        val lastBytes = ByteArray(2)
        val b2 = ByteBuffer.wrap(lastBytes).order(ByteOrder.LITTLE_ENDIAN)
        b2.putShort(opacity)
        l.addAll(lastBytes.toList())
        return l
    }

    fun render(batch: SpriteBatch, sheet: Texture, sprites: List<Sprite>, offsetX: Float, offsetY: Float) {
        sprites[spriteNum.toInt()].render(batch, sheet, offsetX + tlX, offsetY + tlY)
    }

    fun setTransformations(gc: GraphicsContext) {
        val transform = Affine()
        transform.appendTranslation(tlX.toDouble(), tlY.toDouble())
        transform.appendScale(stretchX * 1.0, stretchY * 1.0, 256.0, 256.0)
        transform.appendRotation(rotation * 1.0, 256.0, 256.0)
        gc.globalAlpha = opacity / 255.0
        gc.transform(transform)
    }

    override fun toString(): String {
        return "$spriteNum $duration"
    }
}