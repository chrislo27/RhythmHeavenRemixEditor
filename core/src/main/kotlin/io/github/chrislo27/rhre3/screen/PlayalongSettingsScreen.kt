package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.playalong.PlayalongChars
import io.github.chrislo27.rhre3.playalong.PlayalongControls
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign


class PlayalongSettingsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PlayalongSettingsScreen>(main) {

    companion object {
        private val FILLED_A_BUTTON = PlayalongChars.FILLED_A
        private val BORDERED_A_BUTTON = PlayalongChars.BORDERED_A

        private val CALIBRATION_MUSIC_OFFSET = 0.113f
        private val CALIBRATION_BPM = 120f
        private val CALIBRATION_DURATION_BEATS = 32f
        private val FIVE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.00000", DecimalFormatSymbols())
    }

    override val stage: GenericStage<PlayalongSettingsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val titleLabelIcon: TextLabel<PlayalongSettingsScreen>

    private val controlsLabel: TextLabel<PlayalongSettingsScreen>
    private val playStopButton: Button<PlayalongSettingsScreen>

    private val music: Music = Gdx.audio.newMusic(GitHelper.SOUNDS_DIR.child("etc/input_calibration.ogg"))

    private var calibration: Float = main.preferences.getFloat(PreferenceKeys.PLAYALONG_CALIBRATION, 0f)
    private var summedCalibration = 0f
    private var calibrationInputs = 0

    init {
        val palette = main.uiPalette
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }
        stage.titleLabel.text = "screen.playalongSettings.title"
        stage.titleIcon.visible = false
        titleLabelIcon = TextLabel(palette.copy(ftfont = main.fonts[main.defaultFontLargeKey]), stage, stage).apply {
            this.alignment = Align.topLeft
            this.location.set(this@PlayalongSettingsScreen.stage.titleIcon.location)
            this.text = FILLED_A_BUTTON
            this.isLocalizationKey = false
            this.textWrapping = false
        }
        stage.elements += titleLabelIcon

        val inputCalibrationTitle = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.playalongSettings.calibration"
            this.location.set(screenY = 0.875f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += inputCalibrationTitle
        val inputCalibrationControls = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = Localization["screen.playalongSettings.calibration.controls", FILLED_A_BUTTON]
            this.location.set(screenY = 0.875f, screenHeight = 0.1f)
            this.visible = false
        }
        stage.centreStage.elements += inputCalibrationControls
        stage.centreStage.elements += object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun getRealText(): String {
                return Localization["screen.playalongSettings.calibration.offset", FIVE_DECIMAL_PLACES_FORMATTER.format(calibration.toDouble())]
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenY = 0.775f, screenHeight = 0.1f)
        }
        playStopButton = object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            val playLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_play"))
                this.tint = Color(0f, 0.5f, 0.055f, 1f)
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            }
            val stopLabel = ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_stop"))
                this.tint = Color(242 / 255f, 0.0525f, 0.0525f, 1f)
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.visible = false
            }

            init {
                addLabel(playLabel)
                addLabel(stopLabel)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (!music.isPlaying) {
                    music.play()
                    music.isLooping = true
                    stopLabel.visible = true
                    playLabel.visible = false
                    inputCalibrationTitle.visible = false
                    inputCalibrationControls.visible = true
                } else {
                    music.stop()
                    stopLabel.visible = false
                    playLabel.visible = true
                    inputCalibrationTitle.visible = true
                    inputCalibrationControls.visible = false
                }
            }
        }.apply {
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.035f)
            this.location.set(screenX = 0.5f - location.screenWidth / 2)
        }
        stage.centreStage.elements += playStopButton
        stage.centreStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                resetCalibration()
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.playalongSettings.calibration.reset"
                this.textWrapping = false
            })
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.3f)
            this.location.set(screenX = playStopButton.location.screenX - location.screenWidth - 0.025f)
        }

        controlsLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenHeight = 0.1f)
        }
        stage.centreStage.elements += controlsLabel

        val currentControls = main.playalongControls.copy()
        val isCustom = currentControls !in PlayalongControls.standardControls.values
        val controlsList = (if (isCustom) listOf(PlayalongControls.strCustom to currentControls) else listOf()) + PlayalongControls.standardControls.entries.map { it.toPair() }
        stage.bottomStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.bottomStage, stage.bottomStage) {

            var index: Int = controlsList.indexOfFirst { currentControls == it.second }.coerceAtLeast(0)

            fun cycle(dir: Int) {
                var newIndex = index + dir.sign
                if (newIndex < 0) {
                    newIndex = controlsList.size - 1
                } else if (newIndex >= controlsList.size) {
                    newIndex = 0
                }
                index = newIndex
                main.playalongControls = controlsList[index].second
                updateLabel()
            }

            fun updateLabel() {
                val label = labels.first() as TextLabel
                label.text = Localization["screen.playalongSettings.controls", controlsList[index].first]
                controlsLabel.text = controlsList[index].second.toInputString()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }

            init {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                    this.textWrapping = false
                })
                updateLabel()
            }
        }.apply {
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }
    }

    private fun resetCalibration() {
        calibration = 0f
        summedCalibration = 0f
        calibrationInputs = 0
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        if (music.isPlaying) {
            if (Gdx.input.isKeyJustPressed(main.playalongControls.buttonA)) {
                val musicPos = music.position - CALIBRATION_MUSIC_OFFSET
                val beat = TempoUtils.secondsToBeats(musicPos, CALIBRATION_BPM) % CALIBRATION_DURATION_BEATS
                val beatOffset = beat - beat.roundToInt()
                val secOffset = TempoUtils.beatsToSeconds(beatOffset, CALIBRATION_BPM)
                if (secOffset.absoluteValue <= Playalong.MAX_OFFSET_SEC * 2) {
                    calibrationInputs++
                    summedCalibration += secOffset
                    calibration = summedCalibration / calibrationInputs.coerceAtLeast(1)
                }
            }
        }

        val interval = TempoUtils.beatsToSeconds(2f, 125f)
        val pressDuration = 0.15f
        if (MathHelper.getSawtoothWave(interval) < pressDuration / interval && !music.isPlaying) {
            titleLabelIcon.text = BORDERED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.7f
        } else {
            titleLabelIcon.text = FILLED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.8f
        }
    }

    override fun hide() {
        super.hide()
        music.stop()
        music.dispose()

        main.preferences.putFloat(PreferenceKeys.PLAYALONG_CALIBRATION, calibration).flush()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        music.dispose()
    }

}