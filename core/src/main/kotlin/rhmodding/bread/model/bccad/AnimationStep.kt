package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import rhmodding.bread.model.IAnimationStep
import rhmodding.bread.util.Unknown


class AnimationStep : IAnimationStep {
    
    override var spriteIndex: UShort = 0u
    override var delay: UShort = 1u
    override var stretchX: Float = 1f
    override var stretchY: Float = 1f
    override var opacity: UByte = 255u
    override var rotation: Float = 0f
    
    var depth: Float = 0f
    var translateX: Short = 0
    var translateY: Short = 0
    var color: Color = Color.WHITE
    @Unknown
    var unknown1: Byte = 0
    @Unknown
    var unknown2: Byte = 0
    
    override fun copy(): AnimationStep {
        return AnimationStep().also {
            it.spriteIndex = spriteIndex
            it.delay = delay
            it.stretchX = stretchX
            it.stretchY = stretchY
            it.opacity = opacity
            
            it.depth = depth
            it.translateX = translateX
            it.translateY = translateY
            it.color = color
            it.rotation = rotation
            it.unknown1 = unknown1
            it.unknown2 = unknown2
        }
    }
    
    override fun toString(): String {
        return "AnimationStep=[spriteIndex=$spriteIndex, delay=$delay, stretch=[$stretchX, $stretchY], rotation=$rotation, opacity=$opacity, depth=$depth, translate=[$translateX, $translateY], color=$color, unknown1=0x${unknown1.toUByte().toString(16)}, unknown2=0x${unknown2.toUByte().toString(16)}]"
    }
    
    fun render(batch: SpriteBatch, sheet: Texture, sprites: List<Sprite>, offsetX: Float, offsetY: Float) {
        sprites[spriteIndex.toInt()].render(batch, sheet, offsetX + translateX.toInt(), offsetY + translateY.toInt())
    }

    fun renderWithShader(batch: SpriteBatch, shader: ShaderProgram, sheet: Texture, sprites: List<Sprite>, offsetX: Float, offsetY: Float) {
        sprites[spriteIndex.toInt()].renderWithShader(batch, shader, sheet, offsetX + translateX.toInt(), offsetY + translateY.toInt())
    }
    
}