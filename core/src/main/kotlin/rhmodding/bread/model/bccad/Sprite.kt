package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import rhmodding.bread.model.ISprite


class Sprite : ISprite {
    
    override val parts: MutableList<SpritePart> = mutableListOf()
    
    override fun copy(): Sprite {
        return Sprite().also {
            parts.mapTo(it.parts) { it.copy() }
        }
    }
    
    override fun toString(): String {
        return "Sprite=[numParts=${parts.size}, parts=[${parts.joinToString(separator = "\n")}]]"
    }

    fun render(batch: SpriteBatch, sheet: Texture, offsetX: Float, offsetY: Float) {
        parts.forEach { part ->
            val prevColour = batch.packedColor
            batch.color = part.multColor
            part.render(batch, sheet, offsetX + part.posX.toInt() - 512f, offsetY + (1024 - part.posY.toInt() - 512f))
            batch.packedColor = prevColour
        }
    }

    fun renderWithShader(batch: SpriteBatch, shader: ShaderProgram, sheet: Texture, offsetX: Float, offsetY: Float) {
        parts.forEach { part ->
            val prevColour = batch.packedColor
            batch.color = part.multColor
            part.renderWithShader(batch, shader, sheet, offsetX + part.posX.toInt() - 512f, offsetY + (1024 - part.posY.toInt() - 512f))
            batch.packedColor = prevColour
        }
    }
    
}