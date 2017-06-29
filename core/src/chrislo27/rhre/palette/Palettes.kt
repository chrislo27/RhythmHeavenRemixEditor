package chrislo27.rhre.palette

import chrislo27.rhre.json.PaletteObject
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import java.util.*

abstract class AbstractPalette {

	abstract val editorBg: Color
	val invertedEditorBg: Color by lazy { Color(1f - editorBg.r, 1f - editorBg.g, 1f - editorBg.b, editorBg.a) }
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

	open val cueText: Color = Color(0f, 0f, 0f, 1f)

	fun lerp(a: AbstractPalette, b: AbstractPalette, amt: Float) {
		editorBg.set(a.editorBg).lerp(b.editorBg, amt)
		invertedEditorBg.set(a.invertedEditorBg).lerp(b.invertedEditorBg, amt)
		staffLine.set(a.staffLine).lerp(b.staffLine, amt)
		soundCue.set(a.soundCue).lerp(b.soundCue, amt)
		stretchableSoundCue.set(a.stretchableSoundCue).lerp(b.stretchableSoundCue, amt)
		pattern.set(a.pattern).lerp(b.pattern, amt)
		stretchablePattern.set(a.stretchablePattern).lerp(b.stretchablePattern, amt)
		selectionTint.set(a.selectionTint).lerp(b.selectionTint, amt)
		selectionFill.set(a.selectionFill).lerp(b.selectionFill, amt)
		selectionBorder.set(a.selectionBorder).lerp(b.selectionBorder, amt)
		beatTracker.set(a.beatTracker).lerp(b.beatTracker, amt)
		bpmTracker.set(a.bpmTracker).lerp(b.bpmTracker, amt)
		musicStartTracker.set(a.musicStartTracker).lerp(b.musicStartTracker, amt)
		bpmTrackerSelected.set(a.bpmTrackerSelected).lerp(b.bpmTrackerSelected, amt)
		cueText.set(a.cueText).lerp(b.cueText, amt)
	}

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

	private val rhre0Palette: PaletteObject = JsonHandler.fromJson("""{
  "editorBg": "#FFA57F",
  "staffLine": "#000000",
  "soundCue": "#CFB8AF",
  "stretchableSoundCue": "#FFB8AF",
  "patternCue": "#BBA49B",
  "stretchablePatternCue": "#EBA49B",
  "selectionCueTint": "#DDC7D2",
  "selectionBg": "#B3AE94",
  "selectionBorder": "#19D8D8",
  "beatTracker": "#3FD8D8",
  "bpmTracker": "#00FA00",
  "musicStartTracker": "#FF0000"
}""", PaletteObject::class.java)

	@JvmStatic
	fun toHex(c: Color): String {
		return "#" + c.toString().substring(0, 6).toUpperCase(Locale.ROOT)
	}

	@JvmStatic
	fun getRHRE0Palette(): CustomPalette {
		return getPaletteFromObject(rhre0Palette)
	}

	@JvmStatic
	fun getPaletteFromObject(obj: PaletteObject?): CustomPalette {
		fun String.convertToColor(originalAlpha: Float): Color {
			val hex = this

			if (!hex.startsWith("#") || hex.length != 7)
				throw IllegalArgumentException("Color value must be in the form #FFFFFF (hex)")

			val c = Color.valueOf(hex.substring(1) + "FF")
			c.a = originalAlpha
			return c
		}

		return object : CustomPalette() {
			override val editorBg: Color = obj?.editorBg?.convertToColor(super.editorBg.a) ?: super.editorBg
			override val staffLine: Color = obj?.staffLine?.convertToColor(super.staffLine.a) ?: super.staffLine
			override val soundCue: EntityColors = EntityColors(
					obj?.soundCue?.convertToColor(super.soundCue.bg.a) ?: super.soundCue.bg)
			override val stretchableSoundCue: EntityColors = EntityColors(
					obj?.stretchableSoundCue?.convertToColor(
							super.stretchableSoundCue.bg.a) ?: super.stretchableSoundCue.bg)
			override val pattern: EntityColors = EntityColors(
					obj?.patternCue?.convertToColor(super.pattern.bg.a) ?: super.pattern.bg)
			override val stretchablePattern: EntityColors = EntityColors(
					obj?.stretchablePatternCue?.convertToColor(
							super.stretchablePattern.bg.a) ?: super.stretchablePattern.bg)

			override val selectionTint: Color = obj?.selectionCueTint?.convertToColor(
					super.selectionTint.a) ?: super.selectionTint
			override val selectionFill: Color = obj?.selectionBg?.convertToColor(
					super.selectionFill.a) ?: super.selectionFill
			override val selectionBorder: Color = obj?.selectionBorder?.convertToColor(
					super.selectionBorder.a) ?: super.selectionBorder
			override val beatTracker: Color = obj?.beatTracker?.convertToColor(super.beatTracker.a) ?: super.beatTracker
			override val bpmTracker: Color = obj?.bpmTracker?.convertToColor(super.bpmTracker.a) ?: super.bpmTracker
			override val bpmTrackerSelected: Color = obj?.bpmTrackerSelected?.convertToColor(super.bpmTrackerSelected.a) ?: super.bpmTrackerSelected
			override val musicStartTracker: Color = obj?.musicStartTracker?.convertToColor(
					super.musicStartTracker.a) ?: super.musicStartTracker

			override val cueText: Color = obj?.cueText?.convertToColor(super.cueText.a) ?: super.cueText
		}
	}
}

data class EntityColors(val bg: Color, val outline: Color) {

	constructor(bg: Color) : this(bg, brighten(bg, -0.25f))
	
	fun set(other: EntityColors): EntityColors {
		bg.set(other.bg)
		outline.set(other.outline)
		return this
	}
	
	fun lerp(other: EntityColors, amt: Float): EntityColors {
		bg.lerp(other.bg, amt)
		outline.lerp(other.outline, amt)
		return this
	}

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