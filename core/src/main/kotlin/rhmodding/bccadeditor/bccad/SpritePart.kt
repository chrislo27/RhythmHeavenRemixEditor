package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Scale
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.lang.Math.abs
import java.lang.Math.signum
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
			t.multColor = Color.rgb(buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF)
			t.screenColor = Color.rgb(buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF, buf.get().toInt() and 0xFF)
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
		b.put((multColor.red*255).toByte())
		b.put((multColor.green*255).toByte())
		b.put((multColor.blue*255).toByte())
		b.put((screenColor.red*255).toByte())
		b.put((screenColor.green*255).toByte())
		b.put((screenColor.blue*255).toByte())
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

	fun setTransformations(gc: GraphicsContext) {
		val transform = Affine()
		transform.append(Scale(signum(stretchX)*1.0, signum(stretchY)*1.0, relX - 256.0, relY - 256.0))
		transform.append(Rotate(rotation*signum(stretchX)*signum(stretchY)*1.0, relX - 256 + w*abs(stretchX)/2.0, relY - 256 + h*abs(stretchY)/2.0))
		if (flipX) transform.append(Scale(-1.0, 1.0, relX - 256 + w*abs(stretchX)/2.0, relY - 256 + h*abs(stretchY)/2.0))
		if (flipY) transform.append(Scale(1.0, -1.0, relX - 256 + w*abs(stretchX)/2.0, relY - 256 + h*abs(stretchY)/2.0))
		gc.globalAlpha *= opacity/255.0
		gc.transform(transform)
	}

	fun toImage(original: BufferedImage, multColor: Color): Image {
		var newWidth = abs(original.width*stretchX).toInt()
		var newHeight = abs(original.height*stretchY).toInt()
		if (newWidth < 1) newWidth = 1
		if (newHeight < 1) newHeight = 1
		val resized = BufferedImage(newWidth, newHeight, original.type)
		val g = resized.createGraphics()
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR)
		g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.width,
				original.height, null)
		g.dispose()
		val raster = resized.raster
		val pixels = raster.getPixels(0, 0, raster.width, raster.height, null as IntArray?)
		for (i in 0 until raster.width) {
			for (j in 0 until raster.height) {
				val n = (i + j*raster.width)*4
				val r = pixels[n]/255.0
				val g = pixels[n+1]/255.0
				val b = pixels[n+2]/255.0
				val sr = 1 - (1-screenColor.red)*(1-r)
				val sg = 1 - (1-screenColor.green)*(1-g)
				val sb = 1 - (1-screenColor.blue)*(1-b)
				val mr = r * this.multColor.red
				val mg = g * this.multColor.green
				val mb = b * this.multColor.blue
				pixels[n] = ((sr*(1-r) + r*mr)*multColor.red*255).toInt()
				pixels[n+1] = ((sg*(1-g) + g*mg)*multColor.green*255).toInt()
				pixels[n+2] = ((sb*(1-b) + b*mb)*multColor.blue*255).toInt()
			}
		}
		raster.setPixels(0, 0, raster.width, raster.height, pixels)

		return SwingFXUtils.toFXImage(resized, null)
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
		p.multColor = Color.color(multColor.red, multColor.green, multColor.blue)
		p.screenColor = Color.color(screenColor.red, screenColor.green, screenColor.blue)
		p.unknownData.addAll(unknownData)
		return p
	}
}