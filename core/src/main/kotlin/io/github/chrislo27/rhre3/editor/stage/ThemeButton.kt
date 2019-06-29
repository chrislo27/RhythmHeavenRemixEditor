package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class ThemeButton(val editor: Editor, val editorStage: EditorStage,
                  palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization["editor.theme", LoadedThemes.currentTheme.getRealName()]
        }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val chooserStage = editor.stage.themeChooserStage
        val wasVisible = chooserStage.visible
        editorStage.paneLikeStages.forEach { it.visible = false }
        chooserStage.visible = !wasVisible
        if (chooserStage.visible) {
            chooserStage.resetButtons()
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        editor.main.preferences.putBoolean(PreferenceKeys.THEME_USES_MENU, !editor.main.preferences.getBoolean(PreferenceKeys.THEME_USES_MENU, false)).flush()
    }
}
