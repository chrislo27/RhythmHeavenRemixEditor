package chrislo27.rhre.palette

import com.badlogic.gdx.graphics.Color

abstract class AbstractPalette {

	abstract val editorBg: Color
	abstract val staffLine: Color

	open val selectionFill = Color(0.1f, 0.75f, 0.75f, 0.333f)
	open val selectionBorder = Color(0.1f, 0.85f, 0.85f, 1f)

}

class LightPalette : AbstractPalette() {

	override val editorBg = Color(0.925f, 0.925f, 0.925f, 1f)
	override val staffLine = Color(0f, 0f, 0f, 1f)

}

class DarkPalette : AbstractPalette() {

	override val editorBg = Color(0.1f, 0.1f, 0.1f, 1f)
	override val staffLine = Color(1f, 1f, 1f, 1f)

}
