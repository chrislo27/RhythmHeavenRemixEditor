package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.setHSB


class ViewButton(val editor: Editor, val editorStage: EditorStage,
                 palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    override fun getHoverText(): String {
        return Localization["editor.view", if (editor.views.isEmpty())
            Localization["editor.view.default"]
        else editor.views.joinToString(separator = ", ") { Localization[it.localizationKey] }]
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (labels.isNotEmpty()) {
            val first = labels.first()
            if (first is ImageLabel) {
                if (editor.views.isNotEmpty()) {
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
        val chooserStage = editor.stage.viewChooserStage
        val wasVisible = chooserStage.visible
        editorStage.paneLikeStages.forEach { it.visible = false }
        chooserStage.visible = !wasVisible
    }
}