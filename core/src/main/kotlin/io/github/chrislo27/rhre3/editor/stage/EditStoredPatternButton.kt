package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.patternstorage.FileStoredPattern
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.PatternStoreScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class EditStoredPatternButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    val label = ImageLabel(palette, this, this.stage).apply {
        this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_pencil"))
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization["screen.patternStore.edit.title"]
        }

    init {
        addLabel(label)
        background = false
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val pat = (editorStage.storedPatternsFilter.currentPattern as? FileStoredPattern?) ?: return
        editor.main.screen = PatternStoreScreen(editor.main, editor, pat, null)
    }
}