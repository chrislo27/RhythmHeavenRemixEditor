package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.RemixGeneratorScreen
import io.github.chrislo27.toolboks.ui.*


class RemixGeneratorButton(val editorStage: EditorStage, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    init {
        addLabel(TextLabel(palette, this, this.stage).apply {
            this.textWrapping = false
            this.fontScaleMultiplier = 0.75f
            this.isLocalizationKey = false
            this.text = "RG"
        })
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editorStage.main.screen = RemixGeneratorScreen(editorStage.main, editorStage.editor)
    }
}