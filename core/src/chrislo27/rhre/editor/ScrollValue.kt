package chrislo27.rhre.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils

data class ScrollValue(var game: Int, var pattern: Int, var patternLerp: Float = pattern.toFloat()) {

	fun update() {
		patternLerp = MathUtils.lerp(patternLerp, pattern.toFloat(), Gdx.graphics.deltaTime * 16f)
	}

}
