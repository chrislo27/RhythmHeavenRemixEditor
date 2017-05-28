package chrislo27.rhre.util.message

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import ionium.templates.Main
import ionium.util.Utils

class MessageHandler {

	val list: MutableList<Message> = mutableListOf()

	fun render(batch: Batch, x: Float, y: Float, endX: Float, endY: Float, width: Float = 256f, height: Float = 64f) {
		if (list.isNotEmpty()) {
			val message: Message = list.first()

			message.currentTime += Gdx.graphics.deltaTime

			message.render(batch, x, y, endX, endY, width, height)

			if (message.isExpired()) {
				list.removeAt(0)
			}
		}
	}

}

abstract class Message(val length: Float, var transitionTime: Float = 0.5f) {

	init {
		if (transitionTime * 2 > length)
			throw IllegalArgumentException("Transition time $transitionTime cannot be more than length ($length)")
	}

	open val transitionIn: Interpolation = Interpolation.pow2Out
	open val transitionOut: Interpolation = Interpolation.pow2Out

	var currentTime: Float = 0f

	fun isExpired(): Boolean = currentTime >= length

	val position: Float get() {
		if (currentTime < transitionTime) {
			return transitionIn.apply(0f, 1f, (currentTime / transitionTime).coerceIn(0f, 1f))
		} else if (currentTime > length - transitionTime) {
			return transitionOut.apply(0f, 1f, ((length - currentTime) / transitionTime).coerceIn(0f, 1f))
		}

		return 1f
	}

	abstract fun render(batch: Batch, x: Float, y: Float,
						endX: Float, endY: Float, width: Float, height: Float)

}

open class IconMessage(length: Float, val icon: Texture?, var text: String, val main: chrislo27.rhre.Main,
					   transitionTime: Float = 0.5f,
					   val iconPadding: Float = 4f) : Message(length, transitionTime) {

	constructor(length: Float, icon: Texture?, text: String, main: chrislo27.rhre.Main) : this(length, icon, text, main,
																							   0.5f, 4f)

	override fun render(batch: Batch, x: Float, y: Float,
						endX: Float, endY: Float, width: Float, height: Float) {
		val realX: Float = MathUtils.lerp(x, endX, position)
		val realY: Float = MathUtils.lerp(y, endY, position)

		batch.setColor(0f, 0f, 0f, 0.6f)
		Main.fillRect(batch, realX, realY, width, height)
		batch.setColor(1f, 1f, 1f, 1f)

		val iconSize: Float = if (width >= height) {
			height
		} else {
			width
		}
		val offsetX: Float = if (icon != null) {
			batch.draw(icon, realX + iconPadding, realY + iconPadding, iconSize - iconPadding * 2,
					   iconSize - iconPadding * 2)
			realX + iconPadding + iconSize + iconPadding
		} else {
			realX + iconPadding
		}
		val realWidth: Float = if (icon != null) {
			width - iconPadding * 3 - iconSize
		} else {
			width - iconPadding * 2
		}

		main.font.setColor(1f, 1f, 1f, position)

		var textHeight: Float
		do {
			textHeight = Utils.getHeightWithWrapping(main.font, text,
													 realWidth) + (main.font.lineHeight - main.font.capHeight)
			if (textHeight > height - iconPadding * 2) {
				main.font.data.setScale(main.font.data.scaleX * 0.75f)
				continue
			}
			break
		} while (true)

		main.font.draw(batch, text, offsetX, realY + height / 2f + textHeight / 2f, realWidth, Align.left, true)

		main.font.data.setScale(1f)
		main.font.setColor(1f, 1f, 1f, 1f)
	}

}
