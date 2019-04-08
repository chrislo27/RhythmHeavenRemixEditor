package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.PreferenceKeys.LANGUAGE
import io.github.chrislo27.rhre3.PreferenceKeys.LANG_INDEX
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette
import java.util.*


class LangButton<S : ToolboksScreen<*, *>>(val editor: Editor, palette: UIPalette, parent: UIElement<S>,
                                           stage: Stage<S>)
    : Button<S>(palette, parent, stage), EditorStage.HasHoverText {

    private val main = editor.main

    companion object {
        private val OLD_LANGS: List<LangObj> = listOf(LangObj(""), LangObj("fr"), LangObj("es"), LangObj("de"))
    }

    override fun getHoverText(): String {
        return "${Localization.currentBundle.locale.name}\n${Localization["editor.translationsMayNotBeAccurate"]}"
    }

    private fun persist() {
        val current = Localization.currentBundle
        val str = current.locale.locale.toPrefsString()
        main.preferences.putString(LANGUAGE, str).flush()
    }

    private fun Locale.toPrefsString(): String {
        val obj = LangObj(language, country, variant)
        return JsonHandler.toJson(obj, LangObj::class.java)
    }

    init {
        val prefs = main.preferences
        val jsonStr: String = if (LANG_INDEX in prefs && LANGUAGE !in prefs) {
            // Port over old preferences
            val index = prefs.getInteger(LANG_INDEX, 0).takeIf { it in 0 until OLD_LANGS.size } ?: 0
            prefs.remove(LANG_INDEX)
            val str = JsonHandler.toJson(OLD_LANGS[index], LangObj::class.java)
            Toolboks.LOGGER.info("Porting over language index, got $index, returning $str")
            str
        } else {
            prefs.getString(LANGUAGE, null) ?: JsonHandler.toJson(OLD_LANGS[0], LangObj::class.java)
        }.takeUnless(String::isEmpty) ?: "{}"
        val langObj: LangObj = JsonHandler.fromJson(jsonStr, LangObj::class.java)
        val language: String = langObj.language?.toLowerCase(Locale.ROOT) ?: ""
        val country: String = langObj.country?.toLowerCase(Locale.ROOT) ?: ""
        val variant: String = langObj.variant?.toLowerCase(Locale.ROOT) ?: ""

        Localization.currentBundle = Localization.bundles.find {
            it.locale.locale.language == language && it.locale.locale.country == country && it.locale.locale.variant == variant
        } ?: Localization.bundles.find {
            it.locale.locale.language == language && it.locale.locale.country == country
        } ?: Localization.bundles.find {
            it.locale.locale.language == language
        } ?: Localization.bundles.first()

        persist()
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        Localization.cycle(1)
        persist()
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        Localization.cycle(-1)
        persist()
    }

    class LangObj() {
        var language: String? = null
        var country: String? = null
        var variant: String? = null

        constructor(language: String = "", country: String = "", variant: String = "") : this() {
            this.language = language
            this.country = country
            this.variant = variant
        }
    }

}