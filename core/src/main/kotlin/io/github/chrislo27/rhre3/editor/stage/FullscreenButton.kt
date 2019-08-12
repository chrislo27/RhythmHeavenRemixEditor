package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class FullscreenButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                       stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization[if (Gdx.graphics.isFullscreen) "editor.unfullscreen" else "editor.fullscreen"] + " [LIGHT_GRAY](F11)[]"
        }

    private val imageLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, this.stage).apply {
        this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_fullscreen"))
    }
    private var wasFullscreen: Boolean = false
    
    init {
        addLabel(imageLabel)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)
        val fullscreen = Gdx.graphics.isFullscreen
        if (fullscreen != wasFullscreen) {
            wasFullscreen = fullscreen
            imageLabel.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_${if (fullscreen) "un" else ""}fullscreen"))
        }
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (Gdx.graphics.isFullscreen) {
            editor.main.attemptEndFullscreen()
        } else {
            editor.main.attemptFullscreen()
        }
        editor.main.persistWindowSettings()
    }
}
