package chrislo27.rhre.palette

import chrislo27.rhre.json.PaletteObject
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import java.util.*

abstract class AbstractPalette {

	abstract val editorBg: Color
	abstract val staffLine: Color

	abstract val soundCue: EntityColors
	abstract val stretchableSoundCue: EntityColors
	abstract val pattern: EntityColors
	abstract val stretchablePattern: EntityColors

	open val selectionTint: Color = Color(0f, 0.75f, 0.75f, 1f)
	open val selectionFill: Color = Color(0.1f, 0.75f, 0.75f, 0.333f)
	open val selectionBorder: Color = Color(0.1f, 0.85f, 0.85f, 1f)

	open val beatTracker: Color = Color(0f, 0.9f, 0f, 1f)
	open val bpmTracker: Color = Color(0.4f, 0.4f, 0.9f, 1f)
	open val musicStartTracker: Color = Color(0.9f, 0f, 0f, 1f)
	open val bpmTrackerSelected: Color by lazy { brighten(Color(bpmTracker), 0.5f) }

}

open class LightPalette : AbstractPalette() {

	override val editorBg = Color(0.925f, 0.925f, 0.925f, 1f)
	override val staffLine = Color(0.1f, 0.1f, 0.1f, 0.85f)

	override val soundCue: EntityColors = EntityColors(Color(0.85f, 0.85f, 0.85f, 1f))
	override val stretchableSoundCue: EntityColors = EntityColors(Color(1f, 0.85f, 0.85f, 1f))
	override val pattern: EntityColors = EntityColors(Color(0.85f, 0.85f, 1f, 1f))
	override val stretchablePattern: EntityColors = EntityColors(Color(1f, 0.85f, 1f, 1f))

}

open class DarkPalette : AbstractPalette() {

	override val editorBg = Color(0.15f, 0.15f, 0.15f, 1f)
	override val staffLine = Color(0.95f, 0.95f, 0.95f, 0.85f)

	override val soundCue: EntityColors = EntityColors(Color(0.65f, 0.65f, 0.65f, 1f))
	override val stretchableSoundCue: EntityColors = EntityColors(Color(0.75f, 0.65f, 0.65f, 1f))
	override val pattern: EntityColors = EntityColors(Color(0.75f, 0.75f, 0.9f, 1f))
	override val stretchablePattern: EntityColors = EntityColors(Color(0.75f, 0.65f, 0.75f, 1f))

}

open class HotDogPalette : AbstractPalette() {
	override val editorBg: Color = Color(1f, 1f, 0f, 1f).brighten(0.1f)

	override val staffLine: Color = Color(0f, 0f, 0f, 1f)

	override val soundCue: EntityColors = EntityColors(Color(1f, 0f, 0f, 1f))

	override val stretchableSoundCue: EntityColors = EntityColors(Color(1f, 0f, 0f, 1f))

	override val pattern: EntityColors = EntityColors(Color(1f, 0f, 0f, 1f))

	override val stretchablePattern: EntityColors = EntityColors(Color(1f, 0f, 0f, 1f))

}

abstract class CustomPalette : LightPalette()

object PaletteUtils {
	@JvmStatic
	fun toHex(c: Color): String {
		return "#" + c.toString().substring(0, 6).toUpperCase(Locale.ROOT)
	}

	@JvmStatic
	fun getPaletteFromObject(obj: PaletteObject?): CustomPalette {
		fun String.convertToColor(): Color {
			val hex = this

			if (!hex.startsWith("#") || hex.length != 7)
				throw IllegalArgumentException("Color value must be in the form #FFFFFF (hex)")

			return Color.valueOf(hex.substring(1) + "FF")
		}

		return object : CustomPalette() {
			override val editorBg: Color = obj?.editorBg?.convertToColor() ?: super.editorBg
			override val staffLine: Color = obj?.staffLine?.convertToColor() ?: super.staffLine
			override val soundCue: EntityColors = EntityColors(obj?.soundCue?.convertToColor() ?: super.soundCue.bg)
			override val stretchableSoundCue: EntityColors = EntityColors(
					obj?.stretchableSoundCue?.convertToColor() ?: super.stretchableSoundCue.bg)
			override val pattern: EntityColors = EntityColors(obj?.patternCue?.convertToColor() ?: super.pattern.bg)
			override val stretchablePattern: EntityColors = EntityColors(
					obj?.stretchablePatternCue?.convertToColor() ?: super.stretchablePattern.bg)

			override val selectionTint: Color = obj?.selectionCueTint?.convertToColor() ?: super.selectionTint
			override val selectionFill: Color = obj?.selectionBg?.convertToColor() ?: super.selectionFill
			override val selectionBorder: Color = obj?.selectionBorder?.convertToColor() ?: super.selectionBorder
			override val beatTracker: Color = obj?.beatTracker?.convertToColor() ?: super.beatTracker
			override val bpmTracker: Color = obj?.bpmTracker?.convertToColor() ?: super.bpmTracker
			override val musicStartTracker: Color = obj?.musicStartTracker?.convertToColor() ?: super.musicStartTracker
		}
	}
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

@JvmName("extensionBrighten")
fun com.badlogic.gdx.graphics.Color.brighten(amt: Float): Color {
	val c = chrislo27.rhre.palette.brighten(this, amt)

	return this.set(c)
}