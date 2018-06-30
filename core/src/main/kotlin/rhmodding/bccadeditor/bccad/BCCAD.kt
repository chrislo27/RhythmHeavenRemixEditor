package rhmodding.bccadeditor.bccad

import com.badlogic.gdx.files.FileHandle
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BCCAD(file: FileHandle) {
	var timestamp: Int
	var sheetW: Short
	var sheetH: Short
	val sprites = mutableListOf<Sprite>()
	val animations = mutableListOf<Animation>()

	init {
		val b = file.readBytes()
		val buf = ByteBuffer.wrap(b)
		buf.order(ByteOrder.LITTLE_ENDIAN)
		buf.position(0)
		timestamp = buf.int
		sheetW = buf.short
		sheetH = buf.short
		repeat(buf.int) {
			sprites.add(Sprite.fromBuffer(buf))
		}
		repeat(buf.int) {
			animations.add(Animation.fromBuffer(buf))
		}
	}

	fun toBytes(): ByteArray {
		val firstBytes = ByteArray(12)
		val buf = ByteBuffer.wrap(firstBytes).order(ByteOrder.LITTLE_ENDIAN)
		buf.putInt(timestamp)
		buf.putShort(sheetW)
		buf.putShort(sheetH)
		buf.putInt(sprites.size)
		val l = firstBytes.toMutableList()
		for (s in sprites) {
			l.addAll(s.toBytes())
		}
		val animationSizeBytes = ByteArray(4)
		ByteBuffer.wrap(animationSizeBytes).order(ByteOrder.LITTLE_ENDIAN).putInt(animations.size)
		l.addAll(animationSizeBytes.toList())
		for (a in animations) {
			l.addAll(a.toBytes())
		}
		l.add(0)
		return l.toByteArray()
	}

	override fun toString(): String {
		return "$timestamp $sheetW $sheetH ${sprites.size} ${animations.size}\n" +
				"Sprites: {\n" + sprites.map { "\t" + it.toString() }.joinToString("\n") + "\n}" +
				"\nAnimations: {\n" + animations.map { "\t" + it.toString() }.joinToString("\n") + "\n}"
	}
}