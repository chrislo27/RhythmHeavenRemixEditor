package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Sprite {
    val parts = mutableListOf<SpritePart>()

    companion object {
        fun fromBuffer(buf: ByteBuffer): Sprite {
            val s = Sprite()
            repeat(buf.int) {
                s.parts.add(SpritePart.fromBuffer(buf))
            }
            return s
        }
    }

    fun toBytes(): List<Byte> {
        val firstBytes = ByteArray(4)
        ByteBuffer.wrap(firstBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(parts.size)
        val l = firstBytes.toMutableList()
        for (p in parts) {
            l.addAll(p.toBytes())
        }
        return l
    }

    fun copy(): Sprite {
        val s = Sprite()
        s.parts.addAll(parts.map { it.copy() })
        return s
    }

    fun render(batch: SpriteBatch, sheet: Texture, offsetX: Float, offsetY: Float) {
        parts.forEach { part ->
            val prevColour = batch.packedColor
            batch.color = part.multColor
            part.render(batch, sheet, offsetX + part.relX, offsetY + (1024 - part.relY))
            batch.packedColor = prevColour
        }
    }

    override fun toString(): String {
        return "Textures: {\n" + parts.map { "\t\t" + it.toString() }.joinToString("\n") + "\n\t}"
    }
}