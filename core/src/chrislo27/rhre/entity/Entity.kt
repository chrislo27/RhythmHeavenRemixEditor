package chrislo27.rhre.entity

import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.palette.EntityColors
import chrislo27.rhre.track.PlaybackCompletion
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import ionium.templates.Main

abstract class Entity(val remix: Remix) {
	val bounds = Rectangle()

	var playbackCompletion = PlaybackCompletion.WAITING

	open fun reset() {
		playbackCompletion = PlaybackCompletion.WAITING
	}

	open fun onLengthChange(old: Float) {

	}

	open fun adjustPitch(semitoneChange: Int, min: Int, max: Int) {

	}

	abstract val name: String

	abstract val inspectionFunctions: List<InspectionFunction>

	abstract fun copy(): Entity

	abstract val isStretchable: Boolean

	abstract val isRepitchable: Boolean

	abstract val id: String

	abstract val semitone: Int

	abstract fun render(main: chrislo27.rhre.Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean)

	open fun attemptLoadSounds(): Boolean {
		return false
	}

	fun setBatchColorFromState(batch: SpriteBatch, c: Color, selectionTint: Color, selected: Boolean) {
		batch.color = if (selected)
			tmp.set(c.r * (1 + selectionTint.r), c.g * (1 + selectionTint.g), c.b * (1 + selectionTint.b), c.a)
		else
			c
	}

	protected fun renderRect(batch: SpriteBatch, palette: EntityColors, selectionTint: Color, selected: Boolean,
							 bounds: Rectangle) {
		renderRect(batch, palette.bg, palette.outline, selectionTint, selected, bounds)
	}

	protected fun renderRect(batch: SpriteBatch, bg: Color, outline: Color, selectionTint: Color, selected: Boolean,
							 bounds: Rectangle) {
		setBatchColorFromState(batch, bg, selectionTint, selected)

		Main.fillRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
					  bounds.getHeight() * PX_HEIGHT)
		setBatchColorFromState(batch, outline, selectionTint, selected)
		Main.drawRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
					  bounds.getHeight() * PX_HEIGHT, 4f)

		batch.setColor(1f, 1f, 1f, 1f)
	}

	open fun onStart(delta: Float, intendedStart: Float) {

	}

	open fun onEnd(delta: Float, intendedEnd: Float) {

	}

	open fun onWhile(delta: Float) {

	}

	companion object {
		val PX_HEIGHT = 48
		val PX_WIDTH = PX_HEIGHT * 4
		private val tmp = Color()
		private val tmp2 = Color()
	}

}
