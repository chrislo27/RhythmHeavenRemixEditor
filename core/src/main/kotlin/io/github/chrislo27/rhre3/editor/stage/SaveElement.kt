package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper


class SaveElement(val editor: Editor, val palette: UIPalette, parent: UIElement<EditorScreen>,
                    camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    companion object {
        const val WIDTH_MULTIPLICATION = 12f
        const val FONT_SCALE = 0.8f
    }

    private val icon: ImageLabel<EditorScreen>
    private val label: TextLabel<EditorScreen>

    init {
        icon = ImageLabel(palette, this, this).apply {
            this.location.set(screenX = 0f,
                              screenY = 0f,
                              screenHeight = 1f,
                              screenWidth = 1f / WIDTH_MULTIPLICATION)
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = TextureRegion(AssetRegistry.get<Texture>("weird_odyssey_circle"))
        }
        label = object : TextLabel<EditorScreen>(palette, this, this) {
        }.apply {
            this.location.set(screenX = (1f / WIDTH_MULTIPLICATION) * 1.25f,
                              screenY = 0f,
                              screenHeight = 1f)
            this.location.set(screenWidth = 1f - location.screenX)
            this.isLocalizationKey = false
            this.text = "Autosaving..."
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = FONT_SCALE
        }

        elements.apply {
            add(icon)
            add(label)
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        icon.rotation = MathHelper.getSawtoothWave(2f) * -360f
        super.render(screen, batch, shapeRenderer)
    }

}