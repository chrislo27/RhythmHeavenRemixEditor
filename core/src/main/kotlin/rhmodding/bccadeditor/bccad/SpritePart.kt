package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue


class SpritePart(var x: Short, var y: Short, var w: Short, var h: Short, var relX: Short, var relY: Short) {
    var image: BufferedImage? = null
    var rotation: Float = 0f
    var stretchX: Float = 1f
    var stretchY: Float = 1f
    var flipX: Boolean = false
    var flipY: Boolean = false
    var multColor: Color = Color.WHITE
    var screenColor: Color = Color.BLACK
    var designation: Int = 0
    var unk = 255
    var opacity = 255
    var tldepth = 0f
    var bldepth = 0f
    var trdepth = 0f
    var brdepth = 0f

    val unknownData = mutableListOf<Byte>()

    companion object {
        fun fromBuffer(buf: ByteBuffer): SpritePart {
            val t = SpritePart(buf.short, buf.short, buf.short, buf.short, buf.short, buf.short)
            t.stretchX = buf.float
            t.stretchY = buf.float
            t.rotation = buf.float
            t.flipX = buf.get() == 1.toByte()
            t.flipY = buf.get() == 1.toByte()
            t.multColor = Color((buf.get().toInt() and 0xFF) / 255f, (buf.get().toInt() and 0xFF) / 255f, (buf.get().toInt() and 0xFF) / 255f, 1f)
            t.screenColor = Color((buf.get().toInt() and 0xFF) / 255f, (buf.get().toInt() and 0xFF) / 255f, (buf.get().toInt() and 0xFF) / 255f, 1f)
            t.opacity = buf.get().toInt() and 0xFF
            repeat(12) {
                t.unknownData.add(buf.get())
            }
            t.designation = buf.get().toInt()
            t.unk = buf.short.toInt()
            t.tldepth = buf.float
            t.bldepth = buf.float
            t.trdepth = buf.float
            t.brdepth = buf.float
            return t
        }
    }

    fun toBytes(): List<Byte> {
        val bytes = ByteArray(0x40)
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        b.putShort(x)
        b.putShort(y)
        b.putShort(w)
        b.putShort(h)
        b.putShort(relX)
        b.putShort(relY)
        b.putFloat(stretchX)
        b.putFloat(stretchY)
        b.putFloat(rotation)
        b.put((if (flipX) 1 else 0).toByte())
        b.put((if (flipY) 1 else 0).toByte())
        b.put((multColor.r * 255).toByte())
        b.put((multColor.g * 255).toByte())
        b.put((multColor.b * 255).toByte())
        b.put((screenColor.r * 255).toByte())
        b.put((screenColor.g * 255).toByte())
        b.put((screenColor.b * 255).toByte())
        b.put(opacity.toByte())
        for (i in unknownData) {
            b.put(i)
        }
        b.put(designation.toByte())
        b.putShort(unk.toShort())
        b.putFloat(tldepth)
        b.putFloat(bldepth)
        b.putFloat(trdepth)
        b.putFloat(brdepth)
        return bytes.toList()
    }

    fun render(batch: SpriteBatch, sheet: Texture, offsetX: Float, offsetY: Float) {
        batch.draw(sheet, offsetX, offsetY - h, w / 2f, h / 2f, w * stretchX.absoluteValue, h * stretchY.absoluteValue, stretchX, stretchY, -rotation, x.toInt(), y.toInt(), w.toInt(), h.toInt(), flipX, flipY)
    }

    override fun toString(): String {
        return "($x, $y, $w, $h), rel ($relX, $relY), rot $rotation, stretch ($stretchX, $stretchY)"
    }

    fun copy(): SpritePart {
        val p = SpritePart(x, y, w, h, 512, 512)
        p.relX = relX
        p.relY = relY
        p.stretchX = stretchX
        p.stretchY = stretchY
        p.rotation = rotation
        p.flipX = flipX
        p.flipY = flipY
        p.tldepth = tldepth
        p.trdepth = trdepth
        p.bldepth = bldepth
        p.brdepth = brdepth
        p.multColor = multColor.cpy()
        p.screenColor = screenColor.cpy()
        p.unknownData.addAll(unknownData)
        return p
    }
}