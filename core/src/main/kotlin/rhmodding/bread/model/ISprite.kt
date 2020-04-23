package rhmodding.bread.model

import com.badlogic.gdx.graphics.Color
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import kotlin.math.absoluteValue
import kotlin.math.sign


interface ISprite {
    
    val parts: MutableList<out ISpritePart>
    
    fun copy(): ISprite
    
}

interface ISpritePart {
    
    var regionX: UShort
    var regionY: UShort
    var regionW: UShort
    var regionH: UShort
    
    var posX: Short
    var posY: Short
    
    var stretchX: Float
    var stretchY: Float
    
    var rotation: Float
    
    var flipX: Boolean
    var flipY: Boolean
    
    var opacity: UByte
    
    fun copy(): ISpritePart
    
    fun transform(canvas: Canvas, g: GraphicsContext) {
        g.globalAlpha *= opacity.toInt() / 255.0
        g.transform(Affine().apply {
            append(Scale(stretchX.sign * 1.0, stretchY.sign * 1.0, posX - canvas.width / 2, posY - canvas.height / 2))
            val pivotX = posX - canvas.width / 2 + regionW.toInt() * stretchX.absoluteValue * 0.5
            val pivotY = posY - canvas.height / 2 + regionH.toInt() * stretchY.absoluteValue * 0.5
            append(Rotate(rotation * stretchX.sign * stretchY.sign * 1.0, pivotX, pivotY))
            if (flipX) {
                append(Scale(-1.0, 1.0, pivotX, pivotY))
            }
            if (flipY) {
                append(Scale(1.0, -1.0, pivotX, pivotY))
            }
        })
    }
    
//    fun prepareForRendering(subimage: Image, multColor: Color, graphics: GraphicsContext): Image
    
}
