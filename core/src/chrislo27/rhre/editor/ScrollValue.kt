package chrislo27.rhre.editor

import chrislo27.rhre.registry.GameRegistry
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils

data class ScrollValue(var game: Int, var pattern: Int, var gameScroll: Int = 0,
                       var patternLerp: Float = pattern.toFloat()) {

    fun update() {
        patternLerp = MathUtils.lerp(patternLerp, pattern.toFloat(), Gdx.graphics.deltaTime * 16f)
    }

    fun getMaxGameScroll(): Int {
        return GameRegistry.gamesBySeries.size / (Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y)
    }

}
