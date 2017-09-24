package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*


class TapalongStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    companion object {
        const val AUTO_RESET_SECONDS = 5
        const val MAX_INPUTS = 1024
    }

    var markersEnabled = true
    val seconds = mutableListOf<TapRecord>()
    var tempo: Float = 0f
        private set
    val roundedTempo: Int
        get() = Math.round(tempo)

    private val backgroundPane = ColourPane(this, this).apply {
        this.location.set(0f, 0f, 1f, 1f)
    }
    private val tempoLabel: TextLabel<EditorScreen>
    private val inputsLabel: TextLabel<EditorScreen>
    private val realTempoLabel: TextLabel<EditorScreen>
    private val remix: Remix
        get() = editor.remix
    private var timeSinceLastTap: Long = System.currentTimeMillis()
    private var internalTimekeeper: Float = 0f

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

        tempoLabel = object : TextLabel<EditorScreen>(palette, this, this) {
            override fun getFont(): BitmapFont {
                return this.palette.titleFont
            }
        }.apply {
            this.location.set(screenWidth = 0.15f, screenHeight = 0.4f)
            this.location.set(screenX = 0.5f - this.location.screenWidth / 2,
                              screenY = run {
                                  val buttonHeight = 0.35f
                                  val padding = 0.05f
                                  val totalHeight = buttonHeight + padding * 2
                                  val remainder = 1f - totalHeight

                                  totalHeight + remainder / 2
                              } - this.location.screenHeight / 2)

            this.isLocalizationKey = false
            this.textAlign = Align.center
            this.fontScaleMultiplier = 0.75f
            this.textWrapping = false
        }
        this.elements += tempoLabel
        val quarterNoteLabel = object : TextLabel<EditorScreen>(palette, this, this) {
            override fun getFont(): BitmapFont {
                return this.palette.titleFont
            }
        }.apply {
            this.location.set(screenWidth = 0.1f, screenHeight = tempoLabel.location.screenHeight)
            this.location.set(screenX = tempoLabel.location.screenX - this.location.screenWidth,
                              screenY = tempoLabel.location.screenY)

            this.isLocalizationKey = false
            this.textAlign = Align.right
            this.fontScaleMultiplier = 0.75f
            this.text = "♩="
        }
        this.elements += quarterNoteLabel
        inputsLabel = object : TextLabel<EditorScreen>(palette, this, this) {
            override fun getRealText(): String {
                return Localization[text, seconds.size]
            }
        }.apply {
            this.location.set(screenWidth = 0.25f, screenHeight = tempoLabel.location.screenHeight)
            this.location.set(screenX = (1 / 5f) - this.location.screenWidth / 2,
                              screenY = tempoLabel.location.screenY)

            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "editor.tapalong.numberOfInputs"
        }
        this.elements += inputsLabel
        realTempoLabel = object : TextLabel<EditorScreen>(palette, this, this) {
            override fun getRealText(): String {
                return Localization[text, tempo]
            }
        }.apply {
            this.location.set(screenWidth = 0.25f, screenHeight = tempoLabel.location.screenHeight)
            this.location.set(screenX = (4 / 5f) - this.location.screenWidth / 2,
                              screenY = tempoLabel.location.screenY)

            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "editor.tapalong.avgTempo"
        }
        this.elements += realTempoLabel

        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                reset()
            }
        }.apply {
            addLabel(object : TextLabel<EditorScreen>(palette, this, this@TapalongStage) {
                override fun getRealText(): String {
                    return Localization[text, AUTO_RESET_SECONDS]
                }
            }.apply {
                this.isLocalizationKey = true
                this.text = "editor.tapalong.button.reset"
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenWidth = 0.3f, screenHeight = 0.35f)
            this.location.set(screenX = 0.025f, screenY = 0.05f)
        }
        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                this@TapalongStage.markersEnabled = !this@TapalongStage.markersEnabled
            }
        }.apply {
            addLabel(TextLabel(palette, this, this@TapalongStage).apply {
                this.isLocalizationKey = true
                this.text = "editor.tapalong.button.toggleMarkers"
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenWidth = 0.3f, screenHeight = 0.35f)
            this.location.set(screenX = 0.675f, screenY = 0.05f)

            // FIXME
            this.visible = false
            markersEnabled = false
        }
        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                remix.cuesMuted = !remix.cuesMuted
            }
        }.apply {
            addLabel(object : TextLabel<EditorScreen>(palette, this, this@TapalongStage) {
                override fun getRealText(): String {
                    return Localization["editor.tapalong.button.toggleCues.${remix.cuesMuted}"]
                }
            }.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenWidth = 0.3f, screenHeight = 0.35f)
            this.location.set(screenX = 0.675f, screenY = 0.05f)
        }
        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                tap()
            }
        }.apply {
            addLabel(TextLabel(palette, this, this@TapalongStage).apply {
                this.isLocalizationKey = true
                this.text = "editor.tapalong.button.tap"
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenWidth = 0.3f, screenHeight = 0.35f)
            this.location.set(screenX = 0.35f, screenY = 0.05f)
        }

        this.updatePositions()
        updateLabels()
    }

    private fun tap() {
        if (seconds.isNotEmpty() && (System.currentTimeMillis() - timeSinceLastTap) >= 1000 * AUTO_RESET_SECONDS) {
            reset()
        }

        while (seconds.size >= MAX_INPUTS) {
            seconds.removeAt(0)
        }

        seconds.add(TapRecord(internalTimekeeper,
                              if (remix.playState == PlayState.PLAYING) (remix.seconds - remix.musicStartSec) else null))
        timeSinceLastTap = System.currentTimeMillis()

        // compute new tempo
        if (seconds.size >= 2) {
            seconds.sortBy { it.sec }
            val avgDelta = seconds.drop(1).mapIndexed { index, rec -> rec.sec - seconds[index].sec }.average().toFloat()

            // 120 BPM is 2 beats per second b/c 120 / 60
            // 120 BPM is 0.5 seconds per beat b/c 60 / 120
            // sec = 60 / tempo
            // tempo = 60 / sec
            tempo = 60f / avgDelta
        }

        updateLabels()
    }

    private fun updateLabels() {
        when {
            seconds.isEmpty() -> {
                tempoLabel.text = "0"
            }
            seconds.size == 1 -> {
                tempoLabel.text = Localization["editor.tapalong.first"]
            }
            else -> {
                tempoLabel.text = "$roundedTempo"
            }
        }
    }

    fun reset() {
        seconds.clear()
        tempo = 0f
        updateLabels()
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        backgroundPane.colour.set(screen.editor.theme.background)
        backgroundPane.colour.a = 1f
        internalTimekeeper += Gdx.graphics.deltaTime
        super.render(screen, batch, shapeRenderer)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (visible) {
            if (keycode == Input.Keys.T) {
                tap()
                return true
            } else if (keycode == Input.Keys.R) {
                reset()
                return true
            }
        }

        return false
    }

    data class TapRecord(val sec: Float, val remixSec: Float?)
}