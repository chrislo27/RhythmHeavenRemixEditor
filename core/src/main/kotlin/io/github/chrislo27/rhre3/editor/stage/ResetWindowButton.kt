package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class ResetWindowButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                        stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    override fun getHoverText(): String {
        return Localization["editor.resetwindow"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        Gdx.graphics.setWindowedMode(RHRE3.WIDTH, RHRE3.HEIGHT)
        editor.main.persistWindowSettings()
    }
}