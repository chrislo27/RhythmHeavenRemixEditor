package io.github.chrislo27.toolboks.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.ObjectMap
import io.github.chrislo27.toolboks.Toolboks
import java.util.*
import kotlin.properties.Delegates


/**
 * Pretties up the libGDX localization system.
 */
object Localization {

    val DEFAULT_BASE_HANDLE: FileHandle by lazy {
        Gdx.files.internal("localization/default")
    }
    val DEFAULT_LANG_DEFINITION_FILE: FileHandle by lazy {
        Gdx.files.internal("localization/langs.json")
    }

    var currentIndex: Int by Delegates.observable(0) { _, old, _ ->
        listeners.keys.forEach {
            it.invoke(bundles[old])
        }
    }
    var currentBundle: ToolboksBundle
        get() {
            return bundles[currentIndex]
        }
        set(value) {
            val index = bundles.indexOf(value)
            if (index != -1) {
                currentIndex = index
            }
        }
    val bundles: MutableList<ToolboksBundle> = mutableListOf()
    private val listeners: WeakHashMap<(oldBundle: ToolboksBundle) -> Unit, Unit> = WeakHashMap()
    /**
     * Base, Lang
     */
    private var lastLoadedFiles: Pair<FileHandle, FileHandle> = DEFAULT_BASE_HANDLE to DEFAULT_LANG_DEFINITION_FILE

    fun addListener(listener: (oldBundle: ToolboksBundle) -> Unit) {
        listeners[listener] = Unit
    }

    fun removeListener(listener: (oldBundle: ToolboksBundle) -> Unit) {
        listeners.remove(listener, Unit)
    }

    fun createBundle(locale: NamedLocale, baseHandle: FileHandle = DEFAULT_BASE_HANDLE): ToolboksBundle {
        return ToolboksBundle(locale, I18NBundle.createBundle(baseHandle, locale.locale, "UTF-8"))
    }

    fun getBundlesFromLangFile(langDefFile: FileHandle = DEFAULT_LANG_DEFINITION_FILE,
                               baseHandle: FileHandle = DEFAULT_BASE_HANDLE): List<ToolboksBundle> {
        lastLoadedFiles = baseHandle to langDefFile
        return Json().fromJson(Array<LanguageObject>::class.java, langDefFile)
                .map(LanguageObject::toNamedLocale)
                .map {
                    createBundle(it, baseHandle)
                }
    }

    fun loadBundlesFromLangFile(langDefFile: FileHandle = DEFAULT_LANG_DEFINITION_FILE,
                                baseHandle: FileHandle = DEFAULT_BASE_HANDLE): List<ToolboksBundle> {
        val list = getBundlesFromLangFile(langDefFile, baseHandle)

        bundles.clear()
        bundles.addAll(list)

        return list
    }

    @Suppress("UNCHECKED_CAST")
    fun logMissingLocalizations() {
        val keys: List<String> = bundles.firstOrNull()?.bundle?.let { bundle ->
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            val map = field.get(bundle) as ObjectMap<String, String>

            map.keys().toList()
        } ?: return
        val missing: List<Pair<ToolboksBundle, List<String>>> = bundles.drop(1).map { tbundle ->
            val bundle = tbundle.bundle
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            val map = field.get(bundle) as ObjectMap<String, String>

            tbundle to (keys.map { key ->
                if (!map.containsKey(key)) {
                    key
                } else {
                    ""
                }
            }.filter { !it.isBlank() }).sorted()
        }

        missing.filter { it.second.isNotEmpty() }.forEach {
            Toolboks.LOGGER.warn("Missing keys for bundle ${it.first.locale}:${it.second.joinToString(
                    separator = "") { "\n  * $it" }}")
        }
    }

    fun reloadAll(loadFromLangDef: Boolean) {
        if (!loadFromLangDef) {
            val old = bundles.toList()

            bundles.clear()
            old.mapTo(bundles) {
                createBundle(it.locale)
            }
        } else {
            loadBundlesFromLangFile(lastLoadedFiles.second, lastLoadedFiles.first)
        }
    }

    fun cycle(direction: Int) {
        if (bundles.isEmpty()) {
            error("No bundles found")
        }

        currentIndex = (currentIndex + direction).let {
            (if (it < 0) {
                bundles.size - 1
            } else if (it >= bundles.size) {
                0
            } else {
                it
            })
        }
    }

    private fun checkMissing(key: String): Boolean {
        if (currentBundle.missing[key] != null) {
            return true
        }
        try {
            currentBundle.bundle[key]
        } catch (e: MissingResourceException) {
            currentBundle.missing[key] = true
            Toolboks.LOGGER.warn("Missing content for I18N key $key in bundle ${currentBundle.locale}")

            return true
        }

        return false
    }

    operator fun get(key: String): String {
        if (checkMissing(key))
            return key

        return currentBundle.bundle[key]
    }

    operator fun get(key: String, vararg args: Any?): String {
        if (checkMissing(key))
            return key

        return currentBundle.bundle.format(key, *args)
    }

}