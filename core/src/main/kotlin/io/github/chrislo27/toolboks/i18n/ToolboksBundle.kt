package io.github.chrislo27.toolboks.i18n

import com.badlogic.gdx.utils.I18NBundle


data class ToolboksBundle(val locale: NamedLocale, val bundle: I18NBundle) {

    val missing: MutableMap<String, Boolean> = mutableMapOf()

}