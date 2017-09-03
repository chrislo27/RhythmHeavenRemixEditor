package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.properties.Delegates


class PresentationModeStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val backgroundPane = ColourPane(this, this).apply {
        this.location.set(0f, 0f, 1f, 1f)
    }
    private val remix: Remix
        get() = editor.remix

    private val themePalette: UIPalette = palette.copy(textColor = Color(palette.textColor))
    private val infoTextPalette: UIPalette = palette.copy(textColor = Color(palette.textColor))

    private val progressBar: UIElement<EditorScreen>
    private val timeLabel: TextLabel<EditorScreen>
    private val durationLabel: TextLabel<EditorScreen>
    private val bpmLabel: TextLabel<EditorScreen>

    private var progress = 0f

    init {
        this.elements += backgroundPane
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.location.set(0f, 0f, 1f, 1f)

            this.visible = false
        }

        val fontScale = 0.8f
        this.elements += TextLabel(infoTextPalette, this, this).apply {
            this.location.set(screenHeight = 0.125f, screenY = 0.0f)
            this.textWrapping = false
            this.isLocalizationKey = false
            this.textAlign = Align.center
            this.text = RHRE3.GITHUB
            this.fontScaleMultiplier = fontScale
        }
        this.elements += TextLabel(infoTextPalette, this, this).apply {
            this.location.set(screenHeight = 0.125f, screenY = 0.125f)
            this.textWrapping = false
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "presentation.madeWith"
            this.fontScaleMultiplier = fontScale
        }

        val aboveText = 0.125f + 0.125f

        progressBar = object : UIElement<EditorScreen>(this, this) {
            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                val oldColor = batch.packedColor
                batch.color = themePalette.textColor

                val line = 3f

                batch.drawRect(location.realX, location.realY, location.realWidth, location.realHeight, line)
                batch.fillRect(location.realX + line * 2,
                               location.realY + line * 2,
                               (location.realWidth - line * 4) * progress.coerceIn(0f, 1f),
                               location.realHeight - line * 4)

                batch.setColor(oldColor)
            }
        }.apply {
            this.location.set(screenX = 0.25f, screenY = aboveText + 0.05f, screenWidth = 0.5f, screenHeight = 0.15f)
        }
        this.elements += progressBar
        timeLabel = TextLabel(themePalette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.textAlign = Align.right
            this.text = "0:00"

            this.location.set(screenX = 0f, screenY = progressBar.location.screenY,
                              screenWidth = progressBar.location.screenX - 0.0125f,
                              screenHeight = progressBar.location.screenHeight)
        }
        this.elements += timeLabel
        durationLabel = TextLabel(themePalette, this, this).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.textAlign = Align.left
            this.text = "0:00"

            this.location.set(screenX = progressBar.location.screenX + progressBar.location.screenWidth + 0.0125f,
                              screenY = progressBar.location.screenY,
                              screenHeight = progressBar.location.screenHeight)
            this.location.set(screenWidth = 1f - (this.location.screenX))
        }
        this.elements += durationLabel
        val gameDisplay = object : GameDisplayStage(editor, themePalette, this@PresentationModeStage, this@PresentationModeStage.camera) {
            override fun getFont(): BitmapFont {
                return themePalette.font
            }
        }.apply {
            this@PresentationModeStage.updatePositions()
            this.location.set(screenX = progressBar.location.screenX,
                              screenY = progressBar.location.screenY + progressBar.location.screenHeight + 0.0125f,
                              screenHeight = progressBar.location.screenHeight * 1.5f)
            this.location.set(screenWidth = 1 / 3f)
            this.updatePositions()
        }
        this.elements += gameDisplay
        bpmLabel = object : TextLabel<EditorScreen>(themePalette, this, this){
//            override fun getFont(): BitmapFont {
//                return editor.main.defaultBorderedFont
//            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.textAlign = Align.right
            this.text = "X BPM"
            this.fontScaleMultiplier = GameDisplayStage.FONT_SCALE

            this.location.set(screenX = gameDisplay.location.screenX + gameDisplay.location.screenWidth,
                              screenY = gameDisplay.location.screenY,
                              screenHeight = gameDisplay.location.screenHeight)
            this.location.set(screenWidth = (progressBar.location.screenX + progressBar.location.screenWidth) - (this.location.screenX))
        }
        this.elements += bpmLabel

        this.updatePositions()
    }

    private fun secondsToText(seconds: Int): String {
        val sec = seconds % 60
        return "${seconds / 60}:${if (sec < 10) "0" else ""}$sec"
    }

    private var timeSeconds by Delegates.observable(0) { _, old, new ->
        if (new != old) {
            timeLabel.text = secondsToText(new)
        }
    }
    private var durationSeconds by Delegates.observable(0) { _, old, new ->
        if (new != old) {
            durationLabel.text = secondsToText(new)
        }
    }
    private var bpm by Delegates.observable(Float.NEGATIVE_INFINITY) { _, old, new ->
        if (new != old) {
            bpmLabel.text = "${String.format("%.1f", new)} BPM"
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        backgroundPane.colour.set(screen.editor.theme.background)
        backgroundPane.colour.a = 1f

        themePalette.textColor.set(editor.theme.trackLine)
        infoTextPalette.textColor.set(themePalette.textColor)
        infoTextPalette.textColor.a = 0.75f

        timeSeconds = remix.tempos.beatsToSeconds(remix.beat).toInt()
        durationSeconds = remix.tempos.beatsToSeconds(remix.lastPoint).toInt()
        bpm = remix.tempos.tempoAt(remix.beat)

        progress = (remix.beat / remix.lastPoint).coerceIn(0f, 1f)

        super.render(screen, batch, shapeRenderer)

    }
}