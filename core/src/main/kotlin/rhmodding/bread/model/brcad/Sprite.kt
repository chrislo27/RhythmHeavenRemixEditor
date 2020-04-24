package rhmodding.bread.model.brcad

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import rhmodding.bread.model.ISprite
import rhmodding.bread.util.Unknown
import kotlin.math.sign


class Sprite : ISprite {

    @Unknown
    var unknown: Short = 0

    override val parts: MutableList<SpritePart> = mutableListOf()

    override fun copy(): Sprite {
        return Sprite().also {
            it.unknown = unknown
            parts.mapTo(it.parts) { it.copy() }
        }
    }

    override fun toString(): String {
        return "Sprite=[numParts=${parts.size}, unknown=0x${unknown.toString(16)}, parts=[${parts.joinToString(separator = "\n")}]]"
    }

    fun render(batch: SpriteBatch, sheet: Texture, offsetX: Float, offsetY: Float) {
        parts.forEach { part ->
            val prevColour = batch.packedColor
            batch.packedColor = Color.WHITE_FLOAT_BITS
            part.render(batch, sheet, offsetX + (part.posX.toInt() - 512), offsetY + (1024 - part.posY.toInt() - 512))
            batch.packedColor = prevColour
        }
    }

}