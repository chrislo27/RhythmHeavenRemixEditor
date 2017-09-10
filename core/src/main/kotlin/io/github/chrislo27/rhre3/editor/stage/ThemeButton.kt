package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Themes
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class ThemeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    override fun getHoverText(): String {
        return Localization["editor.palette", LoadedThemes.currentTheme.getRealName()]
    }

    private fun cycle(dir: Int) {
        editor.theme = Themes.defaultThemes.first()
        LoadedThemes.scroll(dir)
        LoadedThemes.persistIndex(editor.main.preferences)
        editor.theme = LoadedThemes.currentTheme
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
//        cycle(1)
        val chooserStage = editor.stage.themeChooserStage
        chooserStage.visible = !chooserStage.visible
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
//        cycle(-1)
    }
}
