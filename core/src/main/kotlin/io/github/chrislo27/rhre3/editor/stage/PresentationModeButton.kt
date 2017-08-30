package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class PresentationModeButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette,
                             parent: UIElement<EditorScreen>,
                             stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    init {
        addLabel(ImageLabel(palette, this, stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_presentation"))
        })
    }

    override fun getHoverText(): String {
        return Localization["editor.presentationmode.info"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val stage = editorStage
        val visible = !stage.presentationModeStage.visible
        stage.elements.filterIsInstance<Stage<*>>().forEach {
            it.visible = !visible
        }
        stage.presentationModeStage.visible = visible
        stage.tapalongStage.visible = false
        stage.buttonBarStage.visible = true
        stage.messageBarStage.visible = true
        if (visible) {
            editor.currentTool = Tool.SELECTION
        }
        stage.updateSelected()
        editor.updateMessageLabel()
    }
}