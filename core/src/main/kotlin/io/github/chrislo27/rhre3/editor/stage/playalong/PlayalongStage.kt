package io.github.chrislo27.rhre3.editor.stage.playalong

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
            this.location.set(0f, 0f, 1f, 1f)
        }
    }

}