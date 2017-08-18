package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class UndoRedoButton(val editor: Editor, val undo: Boolean, palette: UIPalette, parent: UIElement<EditorScreen>,
                     stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val i18nKey = if (undo) "editor.undo" else "editor.redo"

    init {
        addLabel(ImageLabel(palette, this, stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_back")).apply {
                this.flip(!undo, false)
            }
        })
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (undo) {
            this.enabled = editor.remix.canUndo()
        } else {
            this.enabled = editor.remix.canRedo()
        }

        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (undo) {
            if (editor.remix.canUndo()) {
                editor.remix.undo()
            }
        } else {
            if (editor.remix.canRedo()) {
                editor.remix.redo()
            }
        }
    }

    override fun getHoverText(): String {
        return Localization[i18nKey]
    }
}