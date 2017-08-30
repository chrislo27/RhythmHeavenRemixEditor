package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette


class PresentationModeStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val backgroundPane = ColourPane(this, this).apply {
        this.location.set(0f, 0f, 1f, 1f)
    }
    private val remix: Remix
        get() = editor.remix

    init {
        this.elements += backgroundPane
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.location.set(0f, 0f, 1f, 1f)
        }
//        this.elements += object : UIElement<EditorScreen>(this, this) {
//            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
//            }
//
//            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//                return this@TapalongStage.visible
//            }
//
//            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//                return this@TapalongStage.visible
//            }
//
//            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
//                return this@TapalongStage.visible
//            }
//
//            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
//                return this@TapalongStage.visible
//            }
//        }.apply {
//            this.location.set(0f, 0f, 1f, 1f)
//        }



        this.updatePositions()
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        backgroundPane.colour.set(screen.editor.theme.background)
        backgroundPane.colour.a = 1f
        super.render(screen, batch, shapeRenderer)
    }
}