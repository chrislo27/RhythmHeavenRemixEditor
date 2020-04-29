package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import rhmodding.bread.model.ISpritePart
import rhmodding.bread.util.Unknown
import kotlin.math.absoluteValue
import kotlin.math.sign


class SpritePart : ISpritePart {
    
    override var regionX: UShort = 0u
    override var regionY: UShort = 0u
    override var regionW: UShort = 1u
    override var regionH: UShort = 1u
    
    override var posX: Short = 0
    override var posY: Short = 0
    
    override var stretchX: Float = 1f
    override var stretchY: Float = 1f
    
    override var rotation: Float = 0f
    
    override var flipX: Boolean = false
    override var flipY: Boolean = false
    
    override var opacity: UByte = 255u
    
    var multColor: Color = Color.WHITE
    var screenColor: Color = Color.BLACK
    var designation: UByte = 0u
    @Unknown
    var unknown: Short = 0xFF
    var tlDepth: Float = 0f
    var blDepth: Float = 0f
    var trDepth: Float = 0f
    var brDepth: Float = 0f
    
    @Unknown
    var unknownData: ByteArray = ByteArray(12) { 0xFF.toByte() }
    
    override fun copy(): SpritePart {
        return SpritePart().also {
            it.regionX = regionX
            it.regionY = regionY
            it.regionW = regionW
            it.regionH = regionH
            it.posX = posX
            it.posY = posY
            it.stretchX = stretchX
            it.stretchY = stretchY
            it.rotation = rotation
            it.flipX = flipX
            it.flipY = flipY
            it.opacity = opacity
            it.multColor = multColor
            it.screenColor = screenColor
            it.designation = designation
            it.unknown = unknown
            it.tlDepth = tlDepth
            it.blDepth = blDepth
            it.trDepth = trDepth
            it.brDepth = brDepth
            it.unknownData = unknownData.toList().toByteArray()
        }
    }

    fun render(batch: SpriteBatch, sheet: Texture, offsetX: Float, offsetY: Float) {
        batch.draw(sheet, offsetX, offsetY - regionH.toInt() * stretchY, regionW.toInt() / 2f * stretchX, regionH.toInt() / 2f * stretchY, regionW.toInt() * stretchX, regionH.toInt() * stretchY, 1f, 1f, -rotation, regionX.toInt(), regionY.toInt(), regionW.toInt(), regionH.toInt(), flipX, flipY)
    }
    
    fun renderWithShader(batch: SpriteBatch, shader: ShaderProgram, sheet: Texture, offsetX: Float, offsetY: Float) {
        shader.setUniformf("screenColor", screenColor)
        batch.draw(sheet, offsetX, offsetY - regionH.toInt() * stretchY, regionW.toInt() / 2f * stretchX, regionH.toInt() / 2f * stretchY, regionW.toInt() * stretchX, regionH.toInt() * stretchY, 1f, 1f, -rotation, regionX.toInt(), regionY.toInt(), regionW.toInt(), regionH.toInt(), flipX, flipY)
        batch.flush()
        shader.setUniformf("screenColor", Color.BLACK)
    }
    
    override fun toString(): String {
        return "SpritePart[region=[$regionX, $regionY, $regionW, $regionH], pos=[$posX, $posY], stretch=[$stretchX, $stretchY], rotation=$rotation, reflect=[x=$flipX, y=$flipY], opacity=$opacity, multColor=$multColor, screenColor=$screenColor, designation=$designation, tlDepth=$tlDepth, blDepth=$blDepth, trDepth=$trDepth, brDepth=$brDepth, unknown=0x${unknown.toUShort().toString(16)}, unknownData=[${unknownData.joinToString(separator = " ") { it.toUByte().toString(16).padStart(2, '0') }}]]"
        }
    }