package io.github.chrislo27.toolboks.registry

import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen

/**
 * Holds all the screens for a game.
 */
object ScreenRegistry : Disposable {

    val screens: Map<String, ToolboksScreen<*, *>> = mutableMapOf()

    operator fun get(key: String): ToolboksScreen<*, *>? {
        return screens[key]
    }

    fun getNonNull(key: String): ToolboksScreen<*, *> {
        return get(key) ?: throw IllegalArgumentException("No screen found with key $key")
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified S : ToolboksScreen<*, *>> getAsType(key: String): S? {
        return screens[key] as S
    }

    inline fun <reified S : ToolboksScreen<*, *>> getNonNullAsType(key: String): S =
            getAsType<S>(key) ?: throw IllegalArgumentException("No screen found with key $key")

    operator fun plusAssign(pair: Pair<String, ToolboksScreen<*, *>>) {
        add(pair.first, pair.second)
    }

    fun add(key: String, screen: ToolboksScreen<*, *>) {
        if (key.startsWith( Toolboks.TOOLBOKS_ASSET_PREFIX)) {
            throw IllegalArgumentException("$key starts with Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }
        if (screens.containsKey(key)) {
            throw IllegalArgumentException("Already contains key $key")
        }
        (screens as MutableMap)[key] = screen
    }

    fun remove(key: String) {
        (screens as MutableMap).remove(key)?.dispose()
    }

    internal fun addToolboks(keyWithoutPrefix: String, screen: ToolboksScreen<*, *>) {
        if (screens.containsKey(Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix)) {
            throw IllegalArgumentException("Already contains key " + Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix)
        }
        screens as MutableMap
        screens[Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix] = screen
    }

    override fun dispose() {
        screens.values.forEach(Screen::dispose)
    }

}