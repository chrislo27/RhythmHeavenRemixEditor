package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.setHSB


class TapalongToggleButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette,
                           parent: UIElement<EditorScreen>,
                           stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_tapalong_button"))
        })
    }

    override fun getHoverText(): String {
        return Localization["editor.tapalong"] + "\n" + Localization["editor.tapalong.info"]
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (labels.isNotEmpty()) {
            val first = labels.first()
            if (first is ImageLabel) {
                if (editorStage.tapalongStage.visible) {
                    first.tint.setHSB(MathHelper.getSawtoothWave(1.5f), 0.3f, 0.75f)
                } else {
                    first.tint.set(1f, 1f, 1f, 1f)
                }
            }
        }

        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val stage = editorStage
        val visible = !stage.tapalongStage.visible
        stage.elements.filterIsInstance<Stage<*>>().forEach {
            it.visible = !visible
        }
        stage.tapalongStage.visible = visible
        stage.playalongStage.visible = false
        stage.presentationModeStage.visible = false
        stage.paneLikeStages.forEach { it.visible = false }
        stage.buttonBarStage.visible = true
        stage.messageBarStage.visible = true
        if (visible) {
            editor.currentTool = Tool.SELECTION
        }
        stage.updateSelected()
        editor.updateMessageLabel()
    }
}