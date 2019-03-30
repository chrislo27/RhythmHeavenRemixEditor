package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.playalong.*
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign


class PlayalongSettingsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PlayalongSettingsScreen>(main) {

    companion object {
        private val FILLED_A_BUTTON = PlayalongChars.FILLED_A
        private val BORDERED_A_BUTTON = PlayalongChars.BORDERED_A

        private val CALIBRATION_MUSIC_OFFSET = 0.126f
        private val CALIBRATION_BPM = 125f
        private val CALIBRATION_DURATION_BEATS = 32f
        private val FIVE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.00000", DecimalFormatSymbols())

        private var currentController: Controller? = null
    }

    override val stage: GenericStage<PlayalongSettingsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val titleLabelIcon: TextLabel<PlayalongSettingsScreen>

    private val controlsLabel: TextLabel<PlayalongSettingsScreen>
    private val pressedControls: EnumSet<PlayalongInput> = EnumSet.noneOf(PlayalongInput::class.java)
    private val helperPressedControls: EnumSet<PlayalongInput> = EnumSet.noneOf(PlayalongInput::class.java)
    private val playStopButton: Button<PlayalongSettingsScreen>
    private val controllerTitleButton: Button<PlayalongSettingsScreen>
    private val controllerButtonLabel: TextLabel<PlayalongSettingsScreen>
    private val buttonAMapButton: MapButton
    private val buttonBMapButton: MapButton
    private val buttonLeftMapButton: MapButton
    private val buttonRightMapButton: MapButton
    private val buttonUpMapButton: MapButton
    private val buttonDownMapButton: MapButton
    private val allMapButtons: List<MapButton>
    private val cancelMappingButton: Button<PlayalongSettingsScreen>
    private val mappingLabel: TextLabel<PlayalongSettingsScreen>

    private val music: Music get() = AssetRegistry["playalong_settings_input_calibration"]
    private val preferences: Preferences get() = main.preferences

    private inner class Calibration(val key: String, var calibration: Float = main.preferences.getFloat(key, 0f),
                                    var summed: Float = 0f, var inputs: Int = 0) {
        fun reset() {
            summed = 0f
            inputs = 0
            calibration = 0f
        }

        fun compute() {
            calibration = summed / inputs.coerceAtLeast(1)
        }

        fun fireInput() {
            val musicPos = music.position - CALIBRATION_MUSIC_OFFSET
            val beat = TempoUtils.secondsToBeats(musicPos, CALIBRATION_BPM) % CALIBRATION_DURATION_BEATS
            val beatOffset = beat - beat.roundToInt()
            val secOffset = TempoUtils.beatsToSeconds(beatOffset, CALIBRATION_BPM)
            if (secOffset.absoluteValue <= Playalong.MAX_OFFSET_SEC * 2) {
                inputs++
                summed += secOffset
                compute()
            }
        }

        fun persist() {
            main.preferences.putFloat(key, calibration).flush()
        }
    }

    inner class ControllerCalibrationListener(val forController: Controller, val input: ControllerInput) : ControllerAdapter() {
        override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
            if (controller == forController && input is ControllerInput.Button && input.code == buttonIndex && mappingListener == null) {
                keyCalibration.fireInput()
                return true
            }
            return false
        }

        override fun povMoved(controller: Controller, povIndex: Int, value: PovDirection): Boolean {
            if (controller == forController && input is ControllerInput.Pov && input.povCode == povIndex && input.direction == value && mappingListener == null) {
                keyCalibration.fireInput()
                return true
            }
            return false
        }
    }

    inner class ControllerMappingListener(val mapButton: MapButton, val forController: Controller) : ControllerAdapter() {
        private var used = false

        override fun buttonDown(controller: Controller, buttonIndex: Int): Boolean {
            if (used)
                return false
            if (controller == forController) {
                used = true
                onReceiveInput(ControllerInput.Button(buttonIndex))
                Gdx.app.postRunnable {
                    Controllers.removeListener(this)
                }
                return true
            }
            return false
        }

        override fun povMoved(controller: Controller, povIndex: Int, value: PovDirection): Boolean {
            if (used)
                return false
            if (controller == forController && value != PovDirection.center) {
                used = true
                onReceiveInput(ControllerInput.Pov(povIndex, value))
                Gdx.app.postRunnable {
                    Controllers.removeListener(this)
                }
                return true
            }
            return false
        }

        fun onReceiveInput(input: ControllerInput) {
            val currentMapping = Playalong.activeControllerMappings[currentController]
            if (currentMapping != null) {
                mapButton.inputSetter(input, currentMapping)
            }
            cancelMapping()
            targetMapButton(mapButton)
        }
    }

    private val keyCalibration = Calibration(PreferenceKeys.PLAYALONG_CALIBRATION_KEY)
    private val mouseCalibration = Calibration(PreferenceKeys.PLAYALONG_CALIBRATION_MOUSE)
    private var calibrationListener: ControllerCalibrationListener? = null

    private var mappingListener: ControllerMappingListener? = null
    private var currentMapButton: MapButton? = null

    init {
        val palette = main.uiPalette
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }
        stage.titleLabel.text = "screen.playalongSettings.title"
        stage.titleIcon.visible = false
        titleLabelIcon = TextLabel(palette.copy(ftfont = main.defaultFontLargeFTF), stage, stage).apply {
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
        val inputCalibrationControls = object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun canBeClickedOn(): Boolean = true
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (music.isPlaying && isMouseOver() && button == Input.Buttons.LEFT) {
                    mouseCalibration.fireInput()
                    return true
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = Localization["screen.playalongSettings.calibration.controls", FILLED_A_BUTTON]
            this.location.set(screenY = 0.875f, screenHeight = 0.1f)
            this.visible = false
            this.background = true
        }
        stage.centreStage.elements += inputCalibrationControls
        stage.centreStage.elements += object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun getRealText(): String {
                return Localization["screen.playalongSettings.calibration.offset.key", FIVE_DECIMAL_PLACES_FORMATTER.format(keyCalibration.calibration.toDouble())]
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenX = 0f, screenWidth = 0.475f, screenY = 0.775f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TextLabel<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun getRealText(): String {
                return Localization["screen.playalongSettings.calibration.offset.mouse", FIVE_DECIMAL_PLACES_FORMATTER.format(mouseCalibration.calibration.toDouble())]
            }
        }.apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenX = 0.525f, screenWidth = 0.475f, screenY = 0.775f, screenHeight = 0.1f)
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
                    if (calibrationListener != null) {
                        Controllers.removeListener(calibrationListener)
                        calibrationListener = null
                    }

                    val mappings = Playalong.activeControllerMappings
                    val cc = currentController
                    val controllerMapping = mappings[cc]
                    if (cc != null && controllerMapping != null) {
                        calibrationListener = ControllerCalibrationListener(cc, controllerMapping.buttonA)
                        Controllers.addListener(calibrationListener)
                    }
                } else {
                    music.stop()
                    stopLabel.visible = false
                    playLabel.visible = true
                    inputCalibrationTitle.visible = true
                    inputCalibrationControls.visible = false
                    if (calibrationListener != null) {
                        Controllers.removeListener(calibrationListener)
                        calibrationListener = null
                    }
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
                keyCalibration.reset()
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.playalongSettings.calibration.reset.key"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.85f
            })
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.4f)
            this.location.set(screenX = playStopButton.location.screenX - location.screenWidth - 0.025f)
        }
        stage.centreStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                mouseCalibration.reset()
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.playalongSettings.calibration.reset.mouse"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.85f
            })
            this.location.set(screenHeight = 0.1f, screenY = 0.65f)
            this.location.set(screenWidth = 0.4f)
            this.location.set(screenX = playStopButton.location.screenX + playStopButton.location.screenWidth + 0.025f)
        }

        stage.centreStage.elements += ColourPane(stage.centreStage, stage.centreStage).apply {
            this.colour.set(1f, 1f, 1f, 1f)
            val barHeight = 0.005f
            this.location.set(screenY = 0.625f - barHeight / 2, screenHeight = barHeight)
        }

        controlsLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenHeight = 0.1f)
        }
        stage.centreStage.elements += controlsLabel

        val settingsPadding = 0.0125f
        // SFX settings
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.playalongSettings.sfxTitle"
            this.location.set(screenX = 0f, screenY = 0.5f, screenWidth = 0.5f - settingsPadding, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_perfect_fail").play(0.5f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.perfectFailSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f), screenWidth = 0.5f - settingsPadding, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_monster_fail").play(0.5f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.monsterFailSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f) * 2, screenWidth = 0.5f - settingsPadding, screenHeight = 0.1f)
        }
        stage.centreStage.elements += object : TrueCheckbox<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE, checked).flush()
                if (checked) {
                    AssetRegistry.get<Sound>("playalong_sfx_monster_ace").play(1f)
                }
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.playalongSettings.monsterAceSfx"
            }
            this.location.set(screenX = 0f, screenY = 0.5f - (0.1f + 0.025f) * 3, screenWidth = 0.5f - settingsPadding, screenHeight = 0.1f)
        }

        // Separator
        stage.centreStage.elements += ColourPane(stage.centreStage, stage.centreStage).apply {
            this.colour.set(1f, 1f, 1f, 1f)
            val barWidth = 0.00275f
            this.location.set(screenX = 0.5f - barWidth / 2, screenWidth = barWidth, screenY = 0.5f - (0.1f + 0.025f) * 3)
            this.location.set(screenHeight = 0.6f - this.location.screenY)
        }

        // Controllers
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.playalongSettings.controllersTitle"
            this.location.set(screenX = 0.5f + settingsPadding, screenY = 0.5f, screenWidth = 0.5f - settingsPadding, screenHeight = 0.1f)
        }
        // rescan
        val squareWidth = (0.5f - settingsPadding) * 0.075f
        val squareHeight = 0.1f
        stage.centreStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (mappingListener == null) {
                    updateControllers()
                }
            }
        }.apply {
            addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            })
            this.visible = false
            this.location.set(screenX = 0.5f + settingsPadding, screenY = 0.5f - (0.1f + 0.025f), screenWidth = squareWidth, screenHeight = squareHeight)
        }
        controllerTitleButton = object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            fun cycle(dir: Int) {
                val controllers = Playalong.activeControllerMappings.keys.toList()
                if (controllers.isEmpty() || dir == 0) {
                    if (controllers.isEmpty())
                        currentController = null
                    updateCurrentController()
                    return
                }
                val currentController = currentController
                val currentIndex = if (currentController == null) 0 else controllers.indexOf(currentController).coerceAtLeast(0)
                var newIndex = currentIndex + dir.coerceIn(-1, 1)
                newIndex = if (newIndex >= controllers.size) 0 else if (newIndex < 0) controllers.size - 1 else newIndex
                val newController = controllers[newIndex]
                PlayalongSettingsScreen.currentController = newController
                updateCurrentController()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (mappingListener == null)
                    cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                if (mappingListener == null)
                    cycle(-1)
            }
        }.apply {
            this.enabled = false
            this.location.set(screenX = 0.5f + settingsPadding/* + squareWidth + settingsPadding * 0.25f*/, screenY = 0.5f - (0.1f + 0.025f),
                              screenHeight = 0.1f)
            this.location.set(screenWidth = 1f - this.location.screenX)
            controllerButtonLabel = TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 0.75f
                this.textWrapping = false
                this.text = Localization["screen.playalongSettings.noControllers"]
            }
            addLabel(controllerButtonLabel)
        }
        stage.centreStage.elements += controllerTitleButton

        // Mapping buttons
        buttonUpMapButton = MapButton(PlayalongChars.FILLED_JOY_U,
                                      { it.buttonUp }, { i, m -> m.buttonUp = i },
                                      palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding + squareWidth, screenY = 0.25f, screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonUpMapButton
        buttonDownMapButton = MapButton(PlayalongChars.FILLED_JOY_D,
                                        { it.buttonDown }, { i, m -> m.buttonDown = i },
                                        palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding + squareWidth, screenY = 0.125f, screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonDownMapButton
        buttonLeftMapButton = MapButton(PlayalongChars.FILLED_JOY_L,
                                        { it.buttonLeft }, { i, m -> m.buttonLeft = i },
                                        palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding, screenY = 0.125f + 0.0625f, screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonLeftMapButton
        buttonRightMapButton = MapButton(PlayalongChars.FILLED_JOY_R,
                                         { it.buttonRight }, { i, m -> m.buttonRight = i },
                                         palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding + squareWidth * 2, screenY = 0.125f + 0.0625f, screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonRightMapButton
        buttonBMapButton = MapButton(PlayalongChars.FILLED_B,
                                     { it.buttonB }, { i, m -> m.buttonB = i },
                                     palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding + squareWidth * 3.25f, screenY = 0.125f + 0.125f * (1f / 3), screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonBMapButton
        buttonAMapButton = MapButton(PlayalongChars.FILLED_A,
                                     { it.buttonA }, { i, m -> m.buttonA = i },
                                     palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.5f + settingsPadding + squareWidth * 4.25f, screenY = 0.125f + 0.125f * (2f / 3), screenWidth = squareWidth, screenHeight = squareHeight)
        }
        stage.centreStage.elements += buttonAMapButton
        allMapButtons = listOf(buttonAMapButton, buttonBMapButton, buttonUpMapButton, buttonDownMapButton, buttonLeftMapButton, buttonRightMapButton)
        cancelMappingButton = object : Button<PlayalongSettingsScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (mappingListener != null) {
                    val mb = currentMapButton
                    cancelMapping()
                    if (mb != null) {
                        targetMapButton(mb)
                    }
                }
            }
        }.apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.playalongSettings.cancelMapping"
            })
            this.visible = false
            this.location.set(screenX = 0.85f, screenWidth = 0.15f, screenY = 0.125f, screenHeight = squareHeight)
        }
        stage.centreStage.elements += cancelMappingButton
        mappingLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textAlign = Align.left or Align.center
            this.textWrapping = false
            this.fontScaleMultiplier = 0.75f
            this.location.set(screenX = 0.725f, screenWidth = 0.275f, screenY = 0.125f + squareHeight, screenHeight = squareHeight * 1.5f)
        }
        stage.centreStage.elements += mappingLabel

        val currentControls = Playalong.playalongControls.copy()
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
                Playalong.playalongControls = controlsList[index].second
                updateLabel()
            }

            fun updateLabel() {
                val label = labels.first() as TextLabel
                label.text = Localization["screen.playalongSettings.controls", controlsList[index].first]
                updateControlsLabel()
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

        Gdx.app.postRunnable {
            Playalong.loadActiveMappings()
            updateControllers()
        }
    }

    fun updateControlsLabel() {
        controlsLabel.text = Playalong.playalongControls.toInputString(pressedControls)
    }

    fun updateControllers() {
        Playalong.loadActiveMappings()
        updateCurrentController()
    }

    fun updateCurrentController() {
        val mappings = Playalong.activeControllerMappings
        val controllers = setOf<Controller>() //mappings.keys
        controllerTitleButton.enabled = controllers.isNotEmpty()
        mappingLabel.text = ""
        cancelMapping()
        if (controllers.isEmpty()) {
            controllerButtonLabel.text = Localization["screen.playalongSettings.noControllers"]
            currentController = null
            mappingLabel.text = Localization["screen.playalongSettings.noControllers.hint"]
            allMapButtons.forEach { it.enabled = false }
        } else {
            val target = controllers.firstOrNull { it == currentController } ?: controllers.firstOrNull { mappings[it]?.inUse == true } ?: controllers.first()
            mappings.forEach { _, m -> m.inUse = false }
            mappings[target]?.inUse = true
            controllerButtonLabel.text = "${target.name} (${controllers.indexOf(target) + 1}/${controllers.size})"
            currentController = target
            allMapButtons.forEach { it.enabled = true }
        }
        currentMapButton = null
        updateMapButtons()
//        println("Set current controller to ${currentController?.name}")
//        println(Playalong.activeControllerMappings.values.joinToString(separator = ", "))
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        if (music.isPlaying) {
            if (Gdx.input.isKeyJustPressed(Playalong.playalongControls.buttonA)) {
                keyCalibration.fireInput()
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

        helperPressedControls.clear()
        Playalong.activeControllerMappings.forEach { controller, mapping ->
            updatePressedControl(controller, mapping.buttonA, PlayalongInput.BUTTON_A)
            updatePressedControl(controller, mapping.buttonB, PlayalongInput.BUTTON_B)
            updatePressedControl(controller, mapping.buttonLeft, PlayalongInput.BUTTON_DPAD_LEFT)
            updatePressedControl(controller, mapping.buttonRight, PlayalongInput.BUTTON_DPAD_RIGHT)
            updatePressedControl(controller, mapping.buttonDown, PlayalongInput.BUTTON_DPAD_DOWN)
            updatePressedControl(controller, mapping.buttonUp, PlayalongInput.BUTTON_DPAD_UP)
        }
        val controls = Playalong.playalongControls
        updatePressedControl(controls.buttonA, PlayalongInput.BUTTON_A)
        updatePressedControl(controls.buttonB, PlayalongInput.BUTTON_B)
        updatePressedControl(controls.buttonLeft, PlayalongInput.BUTTON_DPAD_LEFT)
        updatePressedControl(controls.buttonRight, PlayalongInput.BUTTON_DPAD_RIGHT)
        updatePressedControl(controls.buttonDown, PlayalongInput.BUTTON_DPAD_DOWN)
        updatePressedControl(controls.buttonUp, PlayalongInput.BUTTON_DPAD_UP)
        if (helperPressedControls != pressedControls) {
            pressedControls.clear()
            pressedControls.addAll(helperPressedControls)
            updateControlsLabel()
        }
    }

    private fun updatePressedControl(keycode: Int, input: PlayalongInput) {
        if (Gdx.input.isKeyPressed(keycode)) {
            helperPressedControls.add(input)
        }
    }

    private fun updatePressedControl(controller: Controller, controllerInput: ControllerInput, input: PlayalongInput) {
        when (controllerInput) {
            is ControllerInput.None -> {
            }
            is ControllerInput.Button -> {
                if (controller.getButton(controllerInput.code)) {
                    helperPressedControls.add(input)
                }
            }
            is ControllerInput.Pov -> {
                val dir = controller.getPov(controllerInput.povCode)
                if (dir == controllerInput.direction) {
                    helperPressedControls.add(input)
                }
            }
        }
    }

    private fun updateMapButtons() {
        val c = currentController
        val m = Playalong.activeControllerMappings[c]
        fun MapButton.checkColor(input: ControllerInput?) {
            this.textLabel.textColor = if (mappingListener?.mapButton == this) Colors.get("RAINBOW") else if (currentMapButton == this) Color.CYAN else if (input?.isNothing() == true) Color.RED else null
        }
        buttonAMapButton.checkColor(m?.buttonA)
        buttonBMapButton.checkColor(m?.buttonB)
        buttonLeftMapButton.checkColor(m?.buttonLeft)
        buttonRightMapButton.checkColor(m?.buttonRight)
        buttonUpMapButton.checkColor(m?.buttonUp)
        buttonDownMapButton.checkColor(m?.buttonDown)
    }

    fun startMapping(mapButton: MapButton) {
        if (mappingListener != null) return
        val forController = currentController ?: return
        mappingListener = ControllerMappingListener(mapButton, forController)
        Controllers.addListener(mappingListener)
        cancelMappingButton.visible = true
        mappingLabel.text = Localization["screen.playalongSettings.awaitMapping"]
        updateMapButtons()
    }

    fun cancelMapping() {
        if (mappingListener != null) {
            Controllers.removeListener(mappingListener)
            mappingListener = null
        }
        cancelMappingButton.visible = false
        currentMapButton = null
        mappingLabel.text = ""
        updateMapButtons()
    }

    fun targetMapButton(mapButton: MapButton) {
        currentMapButton = mapButton
        val currentMapping = Playalong.activeControllerMappings[currentController]
        if (currentMapping != null) {
            mappingLabel.text = "${mapButton.text}: " + mapButton.inputGetter(currentMapping).toString()
            if (!mapButton.inputGetter(currentMapping).isNothing()) {
                mappingLabel.text += "\n${Localization["screen.playalongSettings.clearMapping"]}"
            }
        }
        updateMapButtons()
    }

    override fun hide() {
        super.hide()
        music.stop()
        if (calibrationListener != null) {
            Controllers.removeListener(calibrationListener)
            calibrationListener = null
        }

        cancelMapping()

        keyCalibration.persist()
        mouseCalibration.persist()

        preferences.putString(PreferenceKeys.PLAYALONG_CONTROLS, JsonHandler.toJson(Playalong.playalongControls))
        preferences.putString(PreferenceKeys.PLAYALONG_CONTROLLER_MAPPINGS, JsonHandler.toJson(Playalong.playalongControllerMappings))
        preferences.flush()

        fun mapSfxSettings(vararg key: String): Map<String, Any> {
            return key.associate { it to preferences.getBoolean(it, true) }
        }
        AnalyticsHandler.track("Exit Playalong Settings", mapOf("keyCalibration" to keyCalibration.calibration, "mouseCalibration" to mouseCalibration.calibration) + mapSfxSettings(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE))
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    inner class MapButton(val text: String, val inputGetter: (ControllerMapping) -> ControllerInput,
                          val inputSetter: (ControllerInput, ControllerMapping) -> Unit,
                          palette: UIPalette, parent: UIElement<PlayalongSettingsScreen>, stage: Stage<PlayalongSettingsScreen>)
        : Button<PlayalongSettingsScreen>(palette, parent, stage) {

        val textLabel: TextLabel<PlayalongSettingsScreen>

        init {
            textLabel = TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = this@MapButton.text
            }
            addLabel(textLabel)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            val mappingListener = mappingListener
            if (mappingListener != null) {
                if (mappingListener.mapButton != this) {
                    // Cancel old one
                    cancelMapping()
                    targetMapButton(this)
                }
            } else {
                // Prepare mapping
                if (currentMapButton == this) {
                    startMapping(this)
                } else {
                    targetMapButton(this)
                }
            }
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            // Clear
            if (mappingListener == null) {
                val currentMapping = Playalong.activeControllerMappings[currentController]
                if (currentMapping != null) {
                    inputSetter(ControllerInput.None, currentMapping)
                    cancelMapping()
                    updateMapButtons()
                }
            }
        }
    }

}