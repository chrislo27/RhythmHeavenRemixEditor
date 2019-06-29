package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class JumpToField(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : TextField<EditorScreen>(palette, parent, stage) {

    private var beat: Float = Float.MIN_VALUE

    init {
        canTypeText = { char ->
            (char.isDigit() || char == '-') && text.length < if (text.startsWith("-")) 6 else 5
        }
        canPaste = false
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)
        if (!hasFocus) {
            val oldBeat = Math.round(beat)
            beat = editor.camera.position.x

            if (oldBeat != Math.round(beat) || this.text.isEmpty()) {
                this.text = "${Math.round(beat)}"
            }
        }
    }

    override fun onTextChange(oldText: String) {
        super.onTextChange(oldText)
        if (!hasFocus || editor.remix.playState != PlayState.STOPPED)
            return
        val int = text.toIntOrNull() ?: return
        editor.camera.position.x = int.toFloat()
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization["editor.jumpTo"]
        }
}