package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*


class TapalongToggleButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette,
                           parent: UIElement<EditorScreen>,
                           stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    init {
        addLabel(TextLabel(palette, this, stage).apply {
            this.text = "editor.tapalong"
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.fontScaleMultiplier = 0.75f
            this.textWrapping = true
        })
    }

    override fun getHoverText(): String {
        return Localization["editor.tapalong.info"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val stage = editorStage
        val visible = !stage.tapalongStage.visible
        stage.elements.filterIsInstance<Stage<*>>().forEach {
            it.visible = !visible
        }
        stage.tapalongStage.visible = visible
        stage.buttonBarStage.visible = true
        stage.messageBarStage.visible = true
        if (visible) {
            editor.currentTool = Tool.SELECTION
        }
        stage.updateSelected()
        editor.updateMessageLabel()
    }
}