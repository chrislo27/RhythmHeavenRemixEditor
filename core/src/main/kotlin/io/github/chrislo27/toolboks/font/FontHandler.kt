package io.github.chrislo27.toolboks.font

import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.ToolboksGame


/**
 * A [FreeTypeFont] container. Can handle dynamic resizing.
 */
class FontHandler(val game: ToolboksGame) : Disposable {

    val fonts: MutableMap<String, FreeTypeFont> = mutableMapOf()

    operator fun get(key: String): FreeTypeFont {
        return fonts[key] ?: throw IllegalArgumentException("Font not found: $key")
    }

    operator fun set(key: String, font: FreeTypeFont?) {
        if (font != null) {
            fonts[key] = font
        } else {
            val existing = fonts[key]
            if (existing != null) {
                fonts[key]!!.dispose()
                fonts.remove(key)
            }
        }
    }

    override fun dispose() {
        fonts.values.forEach(Disposable::dispose)
    }

    fun loadAll(width: Float, height: Float) {
        fonts.values.forEach {
            it.load(width, height)
        }
    }

    fun loadFiltered(width: Float, height: Float, filter: (Map.Entry<String, FreeTypeFont>) -> Boolean) {
        fonts.filter(filter).map(Map.Entry<String, FreeTypeFont>::value).forEach {
            it.load(width, height)
        }
    }

    fun loadUnloaded(width: Float, height: Float) {
        loadFiltered(width, height) {
            !it.value.isLoaded()
        }
    }

}