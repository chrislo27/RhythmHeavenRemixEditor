package chrislo27.rhre.palette

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils

abstract class AbstractPalette {

	abstract val editorBg: Color
	abstract val staffLine: Color

	abstract val soundCue: EntityColors
	abstract val stretchableSoundCue: EntityColors
	abstract val bpmSoundCue: EntityColors
	abstract val pattern: EntityColors
	abstract val stretchablePattern: EntityColors

	open val selectionFill = Color(0.1f, 0.75f, 0.75f, 0.333f)
	open val selectionBorder = Color(0.1f, 0.85f, 0.85f, 1f)

}

class LightPalette : AbstractPalette() {

	override val editorBg = Color(0.925f, 0.925f, 0.925f, 1f)
	override val staffLine = Color(0.1f, 0.1f, 0.1f, 0.85f)

	override val soundCue: EntityColors = EntityColors(Color(0.75f, 0.75f, 0.75f, 1f))
	override val stretchableSoundCue: EntityColors = EntityColors(Color(1f, 0.75f, 0.75f, 1f))
	override val bpmSoundCue: EntityColors = EntityColors(Color(0.75f, 1f, 0.75f, 1f))
	override val pattern: EntityColors = EntityColors(Color(0.75f, 0.75f, 1f, 1f))
	override val stretchablePattern: EntityColors = EntityColors(Color(1f, 0.75f, 1f, 1f))

}

class DarkPalette : AbstractPalette() {

	override val editorBg = Color(0.15f, 0.15f, 0.15f, 1f)
	override val staffLine = Color(0.95f, 0.95f, 0.95f, 0.85f)

	override val soundCue: EntityColors = EntityColors(Color(0.5f, 0.5f, 0.5f, 1f))
	override val stretchableSoundCue: EntityColors = EntityColors(Color(0.75f, 0.5f, 0.5f, 1f))
	override val bpmSoundCue: EntityColors = EntityColors(Color(0.5f, 0.75f, 0.5f, 1f))
	override val pattern: EntityColors = EntityColors(Color(0.5f, 0.5f, 0.75f, 1f))
	override val stretchablePattern: EntityColors = EntityColors(Color(0.75f, 0.5f, 0.75f, 1f))

}

data class EntityColors(val bg: Color, val outline: Color) {

	constructor(bg: Color) : this(bg, brighten(bg, -0.25f))

}

private fun brighten(color: Color, amt: Float): Color {
	return Color(MathUtils.clamp(color.r + amt, 0f, 1f),
				 MathUtils.clamp(color.g + amt, 0f, 1f),
				 MathUtils.clamp(color.b + amt, 0f, 1f),
				 color.a)
}