package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class ThemeEditorStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private lateinit var color: Color

    init {
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.colour.a = 0.75f
        }

        // TODO localize

        this.elements += TextLabel(palette, this, this).apply {
            this.location.set(screenX = 0f, screenWidth = 1f, screenY = 0.9f, screenHeight = 0.1f)

            this.textAlign = Align.center
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = "Theme Editor"
        }
    }

}