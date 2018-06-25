package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.PatternDeleteScreen
import io.github.chrislo27.toolboks.ui.*


class DeleteStoredPatternButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    val label = TextLabel(palette, this, this.stage).apply {
        this.isLocalizationKey = false
        this.textAlign = Align.center
        this.textWrapping = false
        this.text = "\uE14C"
    }

    init {
        addLabel(label)
        background = false
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val pat = editorStage.storedPatternsFilter.currentPattern ?: return
        editor.main.screen = PatternDeleteScreen(editor.main, editor, pat)
    }
}