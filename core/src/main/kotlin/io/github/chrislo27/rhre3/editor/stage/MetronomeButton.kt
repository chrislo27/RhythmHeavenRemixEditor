package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper


class MetronomeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                      stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val metronomeFrames: List<TextureRegion> by lazy {
        val tex = AssetRegistry.get<Texture>("ui_icon_metronome")
        val size = 64
        return@lazy listOf(TextureRegion(tex, size * 2, 0, size, size),
                           TextureRegion(tex, size * 3, 0, size, size),
                           TextureRegion(tex, size * 4, 0, size, size),
                           TextureRegion(tex, size * 3, 0, size, size),
                           TextureRegion(tex, size * 2, 0, size, size),
                           TextureRegion(tex, size, 0, size, size),
                           TextureRegion(tex, 0, 0, size, size),
                           TextureRegion(tex, size, 0, size, size)
                          )
    }
    private var start = 0L
    private val label = ImageLabel(palette, this, stage).apply {
        this.image = metronomeFrames[0]
    }

    override fun getHoverText(): String {
        return Localization["editor.metronome"]
    }

    init {
        addLabel(label)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (editor.remix.metronome) {
            val time = if (editor.remix.playState == PlayState.PLAYING)
                (120f / screen.editor.remix.tempos.tempoAt(editor.remix.beat))
            else 1.25f
            label.image = metronomeFrames[(MathHelper.getSawtoothWave(
                    System.currentTimeMillis() - start + ((time + metronomeFrames.size / 2) / metronomeFrames.size * 1000).toInt(), time)
                    * metronomeFrames.size)
                    .toInt().coerceIn(0, metronomeFrames.size - 1)]
        } else {
            label.image = metronomeFrames[0]
        }
        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        start = System.currentTimeMillis()
        editor.remix.metronome = !editor.remix.metronome
    }
}