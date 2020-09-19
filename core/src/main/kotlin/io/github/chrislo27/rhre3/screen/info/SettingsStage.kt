package io.github.chrislo27.rhre3.screen.info

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.VersionHistory
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.editor.CameraBehaviour
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.soundsystem.*
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.version.Version
import javax.sound.sampled.Mixer


class SettingsStage(parent: UIElement<InfoScreen>?, camera: OrthographicCamera, val infoScreen: InfoScreen)
    : Stage<InfoScreen>(parent, camera) {

    private val main: RHRE3Application get() = infoScreen.main
    private val preferences: Preferences get() = infoScreen.preferences
    private val editor: Editor get() = infoScreen.editor
    var didChangeSettings: Boolean = Version.fromStringOrNull(preferences.getString(PreferenceKeys.LAST_VERSION, ""))?.let {
        !it.isUnknown && (it < VersionHistory.ANALYTICS || it < VersionHistory.RE_ADD_STRETCHABLE_TEMPO)
    } ?: false
    
    private var testSoundAudio: BeadsAudio? = null
    private var currentTestSound: BeadsSound? = null

    init {
        val palette = infoScreen.stage.palette
        val padding = 0.025f
        val buttonHeight = 0.1f
        val fontScale = 0.75f
        val settings = this
        val buttonWidth = 0.45f
        // Settings
        // Autosave timer
        settings.elements += object : Button<InfoScreen>(palette, settings, settings) {
            private fun updateText() {
                textLabel.text = Localization["screen.info.autosaveTimer",
                        if (InfoScreen.autosaveTimers[index] == 0) Localization["screen.info.autosaveTimerOff"]
                        else Localization["screen.info.autosaveTimerMin", InfoScreen.autosaveTimers[index]]]
                editor.resetAutosaveTimer()
            }

            private fun persist() {
                preferences.putInteger(PreferenceKeys.SETTINGS_AUTOSAVE, InfoScreen.autosaveTimers[index]).flush()
                didChangeSettings = true
            }

            private var index: Int = run {
                val default = InfoScreen.DEFAULT_AUTOSAVE_TIME
                val pref = preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE, default)
                InfoScreen.autosaveTimers.indexOf(InfoScreen.autosaveTimers.find { it == pref } ?: default).coerceIn(0, InfoScreen.autosaveTimers.size - 1)
            }

            private val textLabel: TextLabel<InfoScreen>
                get() = labels.first() as TextLabel<InfoScreen>

            override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (textLabel.text.isEmpty()) {
                    updateText()
                }
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                index++
                if (index >= InfoScreen.autosaveTimers.size)
                    index = 0

                persist()
                updateText()
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                index--
                if (index < 0)
                    index = InfoScreen.autosaveTimers.size - 1

                persist()
                updateText()
            }

            init {
                Localization.addListener {
                    updateText()
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = false
                this.fontScaleMultiplier = fontScale
            })
            this.tooltipText = "screen.info.autosaveTimer.tooltip"
            this.tooltipTextIsLocalizationKey = true

            this.location.set(screenX = padding,
                              screenY = padding,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Chase camera
        settings.elements += object : Button<InfoScreen>(palette, settings, settings) {
            private val label: TextLabel<InfoScreen> = TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = false
                this.fontScaleMultiplier = fontScale
                this.location.set(pixelX = 2f, pixelWidth = -4f)
                addLabel(this)
            }

            private fun updateText() {
                label.text = Localization["screen.info.cameraBehaviour", Localization[main.settings.cameraBehaviour.localizationKey]]
            }

            private fun cycle(dir: Int) {
                val values = CameraBehaviour.VALUES
                val index = values.indexOf(main.settings.cameraBehaviour) + dir
                val normalized = if (index < 0) values.size - 1 else if (index >= values.size) 0 else index
                main.settings.cameraBehaviour = values[normalized]
                if (dir != 0) {
                    main.settings.persist()
                    didChangeSettings = true
                }
                updateText()
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
                Localization.addListener { updateText() }
                updateText()
            }
        }.apply {
            this.location.set(screenX = padding,
                              screenY = padding * 2 + buttonHeight,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Disable minimap
        settings.elements += FalseCheckbox(palette, settings, settings).apply {
            this.checked = main.settings.disableMinimap
            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.disableMinimap"
            }
            this.leftClickAction = { _, _ ->
                main.settings.disableMinimap = checked
                main.settings.persist()
                didChangeSettings = true
            }
            this.location.set(screenX = padding,
                              screenY = padding * 4 + buttonHeight * 3,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Minimap preview
        settings.elements += object : TrueCheckbox<InfoScreen>(palette, settings, settings) {
            private var bufferSupported = true

            override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (bufferSupported && !editor.stage.minimap.bufferSupported) {
                    bufferSupported = false
                    textLabel.text = "screen.info.minimapPreview.unsupported"
                    textLabel.fontScaleMultiplier = fontScale * fontScale
                    checked = false
                }
                enabled = bufferSupported && !main.settings.disableMinimap

                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (bufferSupported) {
                    main.settings.minimapPreview = checked
                    main.settings.persist()
                    didChangeSettings = true
                } else {
                    main.settings.minimapPreview = false
                    main.settings.persist()
                    preferences.putString(PreferenceKeys.SETTINGS_MINIMAP_PREVIEW, null).flush()
                }
            }
        }.apply {
            this.checked = main.settings.minimapPreview

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textAlign = Align.left
                this.text = "screen.info.minimapPreview"
            }

            this.location.set(screenX = padding,
                              screenY = padding * 3 + buttonHeight * 2,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Subtitle order
        settings.elements += object : TrueCheckbox<InfoScreen>(palette, settings, settings) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                main.settings.subtitlesBelow = checked
                main.settings.persist()
                didChangeSettings = true
            }
        }.apply {
            this.checked = main.settings.subtitlesBelow

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale * 0.9f
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.subtitleOrder"
            }

            this.location.set(screenX = padding,
                              screenY = padding * 5 + buttonHeight * 4,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Smooth dragging
        settings.elements += TrueCheckbox(palette, settings, settings).apply {
            this.checked = main.settings.smoothDragging
            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.smoothDragging"
            }
            this.leftClickAction = { _, _ ->
                main.settings.smoothDragging = checked
                main.settings.persist()
                didChangeSettings = true
            }
            this.location.set(screenX = padding,
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Disable time stretching
        settings.elements += FalseCheckbox(palette, settings, settings).apply {
            this.checked = main.settings.disableTimeStretching

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale * 0.9f
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.disableTimeStretching"
            }

            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = if (SoundStretch.isSupported) "screen.info.disableTimeStretching.tooltip" else "screen.info.disableTimeStretching.notSupported.tooltip"

            this.checkedStateChanged = {
                if (!main.settings.disableTimeStretching && it) {
                    SoundCache.unloadAllDerivatives()
                }
                main.settings.disableTimeStretching = it
                main.settings.persist()
                didChangeSettings = true
            }

            this.location.set(screenX = padding,
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
            this.enabled = SoundStretch.isSupported
        }

        // Discord rich presence
        settings.elements += object : TrueCheckbox<InfoScreen>(palette, settings, settings) {
            val discordIcon = ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_discord"))
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.SETTINGS_DISCORD_RPC_ENABLED, checked).flush()
                didChangeSettings = true
                DiscordHelper.enabled = checked
            }

            override fun computeTextX(): Float {
                return computeCheckWidth() * 2.1f
            }

            override fun onResize(width: Float, height: Float, pixelUnitX: Float, pixelUnitY: Float) {
                super.onResize(width, height, pixelUnitX, pixelUnitY)
                val checkWidth = computeCheckWidth()
                discordIcon.location.set(screenX = checkWidth, screenY = 0f, screenWidth = checkWidth, screenHeight = 1f)
                discordIcon.onResize(this.location.realWidth, this.location.realHeight, pixelUnitX, pixelUnitY)
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_DISCORD_RPC_ENABLED, true)

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.discordRichPresence"
            }

            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)

            addLabel(discordIcon)
        }

        // Glass entities
        settings.elements += object : TrueCheckbox<InfoScreen>(palette, settings, settings) {
            private var bufferSupported = true

            override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (bufferSupported && !editor.glassEffect.fboSupported) {
                    bufferSupported = false
                    textLabel.text = "screen.info.glassEntities.unsupported"
                    textLabel.fontScaleMultiplier = fontScale * fontScale
                    checked = false
                    enabled = false
                }

                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (bufferSupported) {
                    main.settings.glassEntities = checked
                    main.settings.persist()
                    didChangeSettings = true
                } else {
                    main.settings.glassEntities = false
                    main.settings.persist()
                    preferences.putString(PreferenceKeys.SETTINGS_GLASS_ENTITIES, null).flush()
                }
            }
        }.apply {
            this.checked = main.settings.glassEntities

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.glassEntities"
            }

            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }

        // Close warning
        settings.elements += TrueCheckbox(palette, settings, settings).apply {
            this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_CLOSE_WARNING, true)

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale * 0.9f
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.info.closeWarning"
            }

            this.checkedStateChanged = {
                preferences.putBoolean(PreferenceKeys.SETTINGS_CLOSE_WARNING, it)
                didChangeSettings = true
            }

            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 5 + buttonHeight * 4,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        
        // Sound mixer settings
        settings.elements += TextLabel(palette, settings, settings).apply {
            this.isLocalizationKey = true
            this.text = "screen.info.mixerSettings"
            this.textWrapping = false
            this.fontScaleMultiplier = 0.9f
            this.location.set(screenX = 1f - (padding + buttonWidth * 0.917f),
                              screenY = padding * 3 + buttonHeight * 2,
                              screenWidth = buttonWidth * 0.834f,
                              screenHeight = buttonHeight)
        }
        settings.elements += TextLabel(palette, settings, settings).apply {
            this.isLocalizationKey = false
            this.text = "\uE152"
            this.tooltipText = "screen.info.mixerSettings.tooltip"
            this.tooltipTextIsLocalizationKey = true
            this.textWrapping = false
            this.location.set(screenX = 1f - (padding + buttonWidth * 0.083f),
                              screenY = padding * 3 + buttonHeight * 2,
                              screenWidth = buttonWidth * 0.083f,
                              screenHeight = buttonHeight)
        }
        val mixerSettingsLabel = TextLabel(palette, this, this.stage).apply {
            this.isLocalizationKey = false
            this.text = "MIXER INFO NAME"
            this.textWrapping = false
            this.tooltipTextIsLocalizationKey= false
            this.tooltipText = "MIXER INFO TOOLTIP"
            this.fontScaleMultiplier = 0.85f
            this.location.set(screenX = 1f - (padding + buttonWidth * (1f - 0.1f)),
                              screenY = padding * 2 + buttonHeight * 1,
                              screenWidth = buttonWidth * (1f - 0.2f),
                              screenHeight = buttonHeight)
            this.background = true
        }
        settings.elements += mixerSettingsLabel
        val prevMixerButton = Button(palette, settings, settings).apply { 
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = "\uE149"
                this.textWrapping = false
            })
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 2 + buttonHeight * 1,
                              screenWidth = buttonWidth * 0.075f,
                              screenHeight = buttonHeight)
            this.tooltipText = "screen.info.mixerSettings.prev"
            this.tooltipTextIsLocalizationKey = true
        }
        settings.elements += prevMixerButton
        val nextMixerButton = Button(palette, settings, settings).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = "\uE14A"
                this.textWrapping = false
            })
            this.location.set(screenX = 1f - (padding + buttonWidth * 0.075f),
                              screenY = padding * 2 + buttonHeight * 1,
                              screenWidth = buttonWidth * 0.075f,
                              screenHeight = buttonHeight)
            this.tooltipText = "screen.info.mixerSettings.next"
            this.tooltipTextIsLocalizationKey = true
        }
        settings.elements += nextMixerButton
        val resetMixerButton = Button(palette, settings, settings).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "screen.info.mixerSettings.resetToDefault"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 1,
                              screenWidth = buttonWidth * 0.65f,
                              screenHeight = buttonHeight)
            this.tooltipText = "screen.info.mixerSettings.resetToDefault.tooltip"
            this.tooltipTextIsLocalizationKey = true
        }
        settings.elements += resetMixerButton
        val testMixerButton = Button(palette, settings, settings).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "screen.info.mixerSettings.test"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.75f
            })
            this.location.set(screenX = 1f - (padding + buttonWidth * 0.325f),
                              screenY = padding * 1,
                              screenWidth = buttonWidth * 0.325f,
                              screenHeight = buttonHeight)
            this.tooltipText = "screen.info.mixerSettings.test.tooltip"
            this.tooltipTextIsLocalizationKey = true
        }
        settings.elements += testMixerButton
        
        fun updateAudioMixerUI() {
            val currentMixer = BeadsSoundSystem.currentMixer
            val mixerInfo = currentMixer.mixerInfo
            mixerSettingsLabel.text = mixerInfo.name
            mixerSettingsLabel.tooltipText = "${mixerInfo.name}\n${mixerInfo.description}"
        }
        fun playTestSoundToMixer() {
            if (testSoundAudio == null) {
                testSoundAudio = BeadsSoundSystem.newAudio(Gdx.files.internal("sound/mixer_test_sfx.ogg"))
            }
            val audio = testSoundAudio
            if (audio != null && currentTestSound == null) {
                currentTestSound = BeadsSound(audio)
            }
            currentTestSound?.play(false, 1f, 1f, 1f, 0.0)
        }
        fun changeToMixer(mixer: Mixer) {
            val oldMixer = BeadsSoundSystem.currentMixer
            if (mixer !== oldMixer) {
                Toolboks.LOGGER.info("Changing mixer to ${mixer.mixerInfo}")
                val c = currentTestSound
                if (c != null) {
                    c.dispose()
                    currentTestSound = null
                }
                
                BeadsSoundSystem.regenerateAudioContexts(mixer) // !!

                preferences.putString(PreferenceKeys.SETTINGS_AUDIO_MIXER, mixer.mixerInfo.name)
                didChangeSettings = true
                
                Gdx.app.postRunnable {
                    updateAudioMixerUI()
                }
            }
        }
        prevMixerButton.leftClickAction = { _, _ ->
            val mixers = BeadsSoundSystem.supportedMixers
            var i = mixers.indexOf(BeadsSoundSystem.currentMixer)
            i--
            if (i < 0) i = mixers.size - 1
            changeToMixer(mixers[i])
        }
        nextMixerButton.leftClickAction = { _, _ ->
            val mixers = BeadsSoundSystem.supportedMixers
            var i = mixers.indexOf(BeadsSoundSystem.currentMixer)
            i++
            if (i >= mixers.size) i = 0
            changeToMixer(mixers[i])
        }
        testMixerButton.leftClickAction = { _, _ ->
            playTestSoundToMixer()
        }
        resetMixerButton.leftClickAction = { _, _ ->
            changeToMixer(BeadsSoundSystem.getDefaultMixer())
        }
        updateAudioMixerUI()
    }
    
    fun show() {
        BeadsSoundSystem.isRealtime = true
        BeadsSoundSystem.stop()
        BeadsSoundSystem.resume()
    }
    
    fun hide() {
        currentTestSound?.dispose()
        currentTestSound = null
        testSoundAudio = null
        BeadsSoundSystem.stop()
        BeadsSoundSystem.resume()
    }

}