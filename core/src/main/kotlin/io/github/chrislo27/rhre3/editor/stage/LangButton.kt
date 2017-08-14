package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class LangButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val main = editor.main

    override fun getHoverText(): String {
        return "${Localization.currentBundle.locale.name}\n${Localization["editor.translationsmaynotbeaccurate"]}"
    }

    private fun persist() {
        main.preferences.putInteger(PreferenceKeys.LANG_INDEX, Localization.currentIndex).flush()
    }

    init {
        Localization.currentIndex = main.preferences.getInteger(PreferenceKeys.LANG_INDEX, 0)
                .coerceIn(0, Localization.bundles.size - 1)
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

}