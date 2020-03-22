package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.isAltDown
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import kotlin.math.roundToInt


class PlaybackSpeedControl(parent: Stage<EditorScreen>, val editorStage: EditorStage, val palette: UIPalette)
    : Stage<EditorScreen>(parent, parent.camera) {

    private val tex: Texture by lazy { AssetRegistry.get<Texture>("ui_speed_change") }
    private val gradientRegion: TextureRegion by lazy { TextureRegion(tex, 0, 0, 221, 64) }
    private val barRegion: TextureRegion by lazy { TextureRegion(tex, 232, 0, 24, 64) }
    
    private val percentLabel: TextLabel<EditorScreen>
    private val barElement: UIElement<EditorScreen>
    
    private val setSpeeds: List<Float> = listOf(0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f, 0.60f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f,
                                                1f,
                                                1.10f, 1.25f, 1.50f, 1.75f, 2.00f, 2.25f, 2.50f, 2.75f, 3.00f, 4.00f).sorted()
    private val speedZeroIndex: Int = setSpeeds.indexOf(1f)
    private val minSpeed = setSpeeds.first()
    private val maxSpeed = setSpeeds.last()
    
    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
        }
        elements += object : TextLabel<EditorScreen>(palette, this, this) {
            override var tooltipText: String?
                get() = Localization["editor.playbackSpeed.tooltip", "${Editor.TEMPO_DECIMAL_PLACES_FORMATTER.format(editorStage.editor.remix.speedMultiplier.toDouble())}×"]
                set(_) {}
        }.apply {
            this.isLocalizationKey = true
            this.text = "editor.playbackSpeed"
            this.textWrapping = false
            this.fontScaleMultiplier = 0.5f
            this.location.set(screenHeight = 0.45f, screenY = 0.55f, pixelX = 1f, pixelWidth = -2f)
        }
        elements += ImageLabel(palette, this, this).apply {
            this.image = gradientRegion
            this.renderType = ImageLabel.ImageRendering.RENDER_FULL
            this.location.set(screenHeight = 0.6f, screenY = 0f, pixelHeight = -2f)
        }
        percentLabel = TextLabel(palette.copy(ftfont = editorStage.main.defaultBorderedFontFTF), this, this).apply {
            this.isLocalizationKey = false
            this.text = "1.0×"
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = 0.6f
            this.location.set(screenHeight = 0.6f, screenY = 0f, pixelX = 2f, pixelWidth = -4f)
        }
        barElement = object : UIElement<EditorScreen>(this, this) {
            val ratio = 0.375f // Width is this times the height
            private var lastSpeed: Float = -1f

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                val editor = editorStage.editor
                val currentSpeed = editor.remix.speedMultiplier
                if (lastSpeed != currentSpeed) {
                    lastSpeed = currentSpeed
                    
                    // Update percent label text and align
                    percentLabel.textAlign = if (currentSpeed <= 1f) Align.right else Align.left
                    percentLabel.text = "${Editor.TEMPO_DECIMAL_PLACES_FORMATTER.format(currentSpeed.toDouble())}×"
                }
                
                batch.setColor(1f, 1f, 1f, 1f)
                val barWidth = this.location.realHeight * ratio
                val barX = ((if (currentSpeed <= 1f) ((currentSpeed - minSpeed) / (1f - minSpeed) * 0.5f) else ((currentSpeed - 1f) / (maxSpeed - 1f) * 0.5f + 0.5f))).coerceIn(0f, 1f)
                batch.draw(barRegion, this.location.realX + (barX * (this.location.realWidth - barWidth)), this.location.realY,
                           barWidth, this.location.realHeight)
                
                if (isMouseOver() && editor.clickOccupation == ClickOccupation.None) {
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                        val percent = ((stage.camera.getInputX() - location.realX - barWidth / 2) / (location.realWidth - barWidth)).coerceIn(0f, 1f)
                        if (Gdx.input.isShiftDown() && !Gdx.input.isControlDown() && !Gdx.input.isAltDown()) {
                            editor.remix.speedMultiplier = if (percent <= 0.5f) MathUtils.lerp(minSpeed, 1f, percent * 2) else MathUtils.lerp(1f, maxSpeed, (percent - 0.5f) * 2)
                        } else {
                            var index = 0
                            var lastSpeedPercent = 0f
                            for (i in 1 until setSpeeds.size) {
                                val sp = setSpeeds[i]
                                val spPercent = if (sp <= 1f) ((sp - minSpeed) / (1f - minSpeed) * 0.5f) else ((sp - 1f) / (maxSpeed - 1f) * 0.5f + 0.5f)
                                val halfway = MathUtils.lerp(lastSpeedPercent, spPercent, 0.5f)
                                if (percent > halfway) {
                                    lastSpeedPercent = spPercent
                                    index = i
                                } else {
                                    break
                                }
                            }
                            editor.remix.speedMultiplier = setSpeeds[index]
                        }
                    } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                        editor.remix.speedMultiplier = 1f
                    }
                }
            }

            override fun scrolled(amount: Int): Boolean {
                val editor = editorStage.editor
                if (isMouseOver() && editor.clickOccupation == ClickOccupation.None) {
                    var index: Int = 0
                    for (i in 1 until setSpeeds.size) {
                        if (setSpeeds[i] <= editor.remix.speedMultiplier) {
                            index = i
                        } else break
                    }
                    if (index == -1) index = speedZeroIndex
                    index += -amount
                    index = index.coerceIn(0, setSpeeds.size - 1)
                    editor.remix.speedMultiplier = setSpeeds[index]
                    return true
                }
                return false
            }
        }.apply {
            this.location.set(screenHeight = 0.6f, screenY = 0f)
        }
        elements += barElement
        elements += percentLabel
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)
        if (editorStage.playalongStage.visible || editorStage.tapalongStage.visible) {
            if (this.visible) {
                this.visible = false
                editorStage.editor.remix.speedMultiplier = 1f
            }
        } else {
            this.visible = true
        }
    }
}