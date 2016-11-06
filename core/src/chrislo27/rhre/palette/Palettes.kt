package chrislo27.rhre.palette

import com.badlogic.gdx.graphics.Color

abstract class AbstractPalette {

	abstract val editorBg: Color

	val selectionFill = Color(0.1f, 0.75f, 0.75f, 0.333f)
	val selectionBorder = Color(0.1f, 0.85f, 0.85f, 1f)

}

class LightPalette : AbstractPalette() {

	override val editorBg = Color(0.95f, 0.95f, 0.95f, 1f)

}

class DarkPalette : AbstractPalette() {

	override val editorBg = Color(0.1f, 0.1f, 0.1f, 1f)

}
