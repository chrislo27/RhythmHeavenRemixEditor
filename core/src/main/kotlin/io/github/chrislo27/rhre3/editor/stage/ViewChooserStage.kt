package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


class ViewChooserStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

}