package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class PanButton(val editor: Editor, val left: Boolean,
                palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_right_chevron")).apply {
                flip(left, false)
            }
            this.renderType = ImageLabel.ImageRendering.RENDER_FULL
        })

        this.background = false
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)

        if (isMouseOver() && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && visible && enabled && wasClickedOn) {
            val camera = editor.camera
            camera.position.x += (if (left) -1 else 1) * Gdx.graphics.deltaTime * 5f
            camera.update()
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)

        val camera = editor.camera
        camera.position.x = if (left) {
            0f
        } else {
            editor.remix.getLastEntityPoint()
        }
        camera.update()
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization[if (left) "editor.pan.left" else "editor.pan.right"]
        }
}
