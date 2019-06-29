package io.github.chrislo27.rhre3.editor.stage.advopt

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class CopyGamesUsedButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                          stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    companion object {
        val strings: List<String> = listOf("Copy\ngames", "[CYAN]Copied![]", "No\ngames...")
    }

    init {
        this.visible = false
    }

    private var resetTextIn: Float = 0f

    private val label: TextLabel<EditorScreen> = TextLabel(palette, this, stage).apply {
        this@CopyGamesUsedButton.addLabel(this)
        this.fontScaleMultiplier = 0.4f
        this.text = strings[0]
        this.isLocalizationKey = false
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return when (label.text) {
                strings[1] -> "Copied successfully to clipboard!"
                strings[2] -> "No games in remix"
                else -> "Click to copy games used to clipboard"
            }
        }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)
        if (resetTextIn > 0) {
            resetTextIn -= Gdx.graphics.deltaTime
            if (resetTextIn <= 0) {
                label.text = strings[0]
            }
        }
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        this.visible = editor.main.advancedOptions
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)

        val games = editor.getGamesUsedInRemix()

        if (games.isEmpty()) {
            label.text = strings[2]
        } else {
            label.text = strings[1]
            Gdx.app.clipboard.contents = games
        }

        resetTextIn = 3f
    }
}