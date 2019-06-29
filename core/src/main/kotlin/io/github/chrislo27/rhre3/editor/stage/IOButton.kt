package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.OpenRemixScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class IOButton(val editor: Editor, val screen: String, val localization: String,
               palette: UIPalette, parent: UIElement<EditorScreen>,
               stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization[localization]
        }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val nextScreen = ScreenRegistry.getNonNull(screen)
        editor.main.screen = nextScreen

        if (nextScreen is OpenRemixScreen) {
            nextScreen.attemptOpenPicker()
        }
    }
}