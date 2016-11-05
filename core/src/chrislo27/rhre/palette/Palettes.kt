package chrislo27.rhre.palette

import com.badlogic.gdx.graphics.Color

abstract class AbstractPalette {

	abstract val editorBg: Color

}

class LightPalette : AbstractPalette() {

	override val editorBg: Color = Color(0.95f, 0.95f, 0.95f, 1f)

}

class DarkPalette : AbstractPalette() {

	override val editorBg: Color = Color(0.1f, 0.1f, 0.1f, 1f)

}
