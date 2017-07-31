package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class EditorStage(parent: UIElement<EditorScreen>?,
                  camera: OrthographicCamera, val main: RHRE3Application)
    : Stage<EditorScreen>(parent, camera), Palettable {

    override var palette: UIPalette = main.uiPalette
    val messageBarStage: Stage<EditorScreen>

    init {
        messageBarStage = Stage(this, camera).apply {
            this.location.set(0f, 0f,
                              1f, Editor.MESSAGE_BAR_HEIGHT / RHRE3.HEIGHT.toFloat())
        }
        elements += messageBarStage
        run {
            messageBarStage.updatePositions()
            messageBarStage.elements +=
                    ColourPane(messageBarStage, messageBarStage).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                    }
            messageBarStage.elements +=
                    TextLabel(palette, messageBarStage, messageBarStage).apply {
                        this.fontScaleMultiplier = 0.5f
                        this.textAlign = Align.bottomLeft
                        this.textWrapping = false
                        this.location.set(0f, -0.5f,
                                          1f - (main.versionTextWidth / RHRE3.WIDTH),
                                          1.5f,
                                          pixelWidth = -8f)
                        this.setText("test test hahehheheaduwahdha")
                    }
        }

        this.updatePositions()
    }

}
