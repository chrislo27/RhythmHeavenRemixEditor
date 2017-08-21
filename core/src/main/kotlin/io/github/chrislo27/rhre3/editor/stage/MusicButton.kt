package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*


class MusicButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private var wasMuted: Boolean? = null

    private val icons: List<TextureRegion> = listOf(
            TextureRegion(AssetRegistry.get<Texture>("ui_icon_music_button")),
            TextureRegion(AssetRegistry.get<Texture>("ui_icon_music_button_muted"))
                                                   )
    private val label = ImageLabel(palette, this, stage)

    init {
        addLabel(label)
    }

    override fun getHoverText(): String {
        return Localization["editor.music"]
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val current = editor.remix.isMusicMuted
        if (wasMuted != current) {
            wasMuted = current

            label.image = icons[if (current) 1 else 0]
        }

        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.main.screen = ScreenRegistry.getNonNull("musicSelect")
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        editor.remix.isMusicMuted = !editor.remix.isMusicMuted
    }
}