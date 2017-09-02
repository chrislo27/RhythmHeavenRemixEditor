package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class PresentationModeStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val backgroundPane = ColourPane(this, this).apply {
        this.location.set(0f, 0f, 1f, 1f)
    }
    private val remix: Remix
        get() = editor.remix

    private val themePalette: UIPalette = palette.copy(textColor = Color(palette.textColor))
    private val infoTextPalette: UIPalette = palette.copy(textColor = Color(palette.textColor))

    init {
        this.elements += backgroundPane
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.location.set(0f, 0f, 1f, 1f)

            this.visible = false
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

        val fontScale = 0.8f
        this.elements += TextLabel(infoTextPalette, this, this).apply {
            this.location.set(screenHeight = 0.125f, screenY = 0.05f)
            this.textWrapping = false
            this.isLocalizationKey = false
            this.textAlign = Align.center
            this.text = RHRE3.GITHUB
            this.fontScaleMultiplier = fontScale
        }
        this.elements += TextLabel(infoTextPalette, this, this).apply {
            this.location.set(screenHeight = 0.125f, screenY = 0.175f)
            this.textWrapping = false
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "presentation.madeWith"
            this.fontScaleMultiplier = fontScale
        }

        val aboveText = 0.125f + 0.175f

        this.updatePositions()
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        backgroundPane.colour.set(screen.editor.theme.background)
        backgroundPane.colour.a = 1f

        themePalette.textColor.set(editor.theme.trackLine)
        infoTextPalette.textColor.set(themePalette.textColor)
        infoTextPalette.textColor.a = 0.75f

        super.render(screen, batch, shapeRenderer)
    }
}