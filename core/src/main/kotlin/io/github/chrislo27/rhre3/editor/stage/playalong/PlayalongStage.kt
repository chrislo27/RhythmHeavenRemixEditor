package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class PlayalongStage(val editor: Editor, val editorStage: EditorStage,
                     val palette: UIPalette, parent: UIElement<EditorScreen>?, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    init {
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.location.set(screenHeight = 0.25f)
            this.location.set(0f, 0f, 1f)
        }

        this.elements += FlickingStage(this, this).apply {
            this.colour.set(Color.valueOf("00BC67"))
            this.location.set(screenX = 0.6f)
            this.location.set(location.screenX, 0f, 1f - location.screenX, 1f)
            this.visible = false
        }
    }

}