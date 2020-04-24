package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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
        batch.draw(sheet, offsetX, offsetY - regionH.toInt(), regionW.toInt() / 2f * stretchX.sign, regionH.toInt() / 2f * stretchY.sign, regionW.toInt() * stretchX, regionH.toInt() * stretchY, 1f, 1f, -rotation, regionX.toInt(), regionY.toInt(), regionW.toInt(), regionH.toInt(), flipX, flipY)
    }
    
//    override fun prepareForRendering(subimage: Image, multColor: Color, graphics: GraphicsContext): Image {
//        val width = subimage.width.toInt()
//        val height = subimage.height.toInt()
//        val pixels: IntArray = IntArray(4)
//        val wrImg = WritableImage(width, height)
//        val pixelReader = subimage.pixelReader
//        val pixelWriter = wrImg.pixelWriter
//        for (i in 0 until width) {
//            for (j in 0 until height) {
//                val argb = pixelReader.getArgb(i, j)
//                pixels[0] = (argb shr 24) and 0xFF
//                pixels[1] = (argb shr 16) and 0xFF
//                pixels[2] = (argb shr 8) and 0xFF
//                pixels[3] = argb and 0xFF
//                val r = pixels[1] / 255.0
//                val g = pixels[2] / 255.0
//                val b = pixels[3] / 255.0
//                val sr = 1 - (1 - screenColor.red) * (1 - r)
//                val sg = 1 - (1 - screenColor.green) * (1 - g)
//                val sb = 1 - (1 - screenColor.blue) * (1 - b)
//                val mr = r * this.multColor.red
//                val mg = g * this.multColor.green
//                val mb = b * this.multColor.blue
//                pixels[1] = ((sr * (1 - r) + r * mr) * multColor.red * 255).toInt()
//                pixels[2] = ((sg * (1 - g) + g * mg) * multColor.green * 255).toInt()
//                pixels[3] = ((sb * (1 - b) + b * mb) * multColor.blue * 255).toInt()
//                pixelWriter.setArgb(i, j, (pixels[0] shl 24) or (pixels[1] shl 16) or (pixels[2] shl 8) or (pixels[3]))
//            }
//        }
//        return wrImg
//    }

//    override fun createFXSubimage(texture: BufferedImage, regionSubimage: BufferedImage, multColor: Color): Image {
//        val newWidth = abs(regionSubimage.width * stretchX).toInt().coerceAtLeast(1)
//        val newHeight = abs(regionSubimage.height * stretchY).toInt().coerceAtLeast(1)
//        val resized = BufferedImage(newWidth, newHeight, texture.type)
//        val g = resized.createGraphics()
//        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
//        g.drawImage(regionSubimage, 0, 0, newWidth, newHeight, 0, 0, regionSubimage.width,
//                    regionSubimage.height, null)
//        g.dispose()
//        val raster = resized.raster
//        val pixels = raster.getPixels(0, 0, raster.width, raster.height, null as IntArray?)
//        for (i in 0 until raster.width) {
//            for (j in 0 until raster.height) {
//                val n = (i + j * raster.width) * 4
//                val r = pixels[n] / 255.0
//                val g = pixels[n + 1] / 255.0
//                val b = pixels[n + 2] / 255.0
//                val sr = 1 - (1 - screenColor.red) * (1 - r)
//                val sg = 1 - (1 - screenColor.green) * (1 - g)
//                val sb = 1 - (1 - screenColor.blue) * (1 - b)
//                val mr = r * this.multColor.red
//                val mg = g * this.multColor.green
//                val mb = b * this.multColor.blue
//                pixels[n] = ((sr * (1 - r) + r * mr) * multColor.red * 255).toInt()
//                pixels[n + 1] = ((sg * (1 - g) + g * mg) * multColor.green * 255).toInt()
//                pixels[n + 2] = ((sb * (1 - b) + b * mb) * multColor.blue * 255).toInt()
//            }
//        }
//        raster.setPixels(0, 0, raster.width, raster.height, pixels)
//
//        return SwingFXUtils.toFXImage(resized, null)
//    }
    
    override fun toString(): String {
        return "SpritePart[region=[$regionX, $regionY, $regionW, $regionH], pos=[$posX, $posY], stretch=[$stretchX, $stretchY], rotation=$rotation, reflect=[x=$flipX, y=$flipY], opacity=$opacity, multColor=$multColor, screenColor=$screenColor, designation=$designation, tlDepth=$tlDepth, blDepth=$blDepth, trDepth=$trDepth, brDepth=$brDepth, unknown=0x${unknown.toUShort().toString(16)}, unknownData=[${unknownData.joinToString(separator = " ") { it.toUByte().toString(16).padStart(2, '0') }}]]"
        }
    }