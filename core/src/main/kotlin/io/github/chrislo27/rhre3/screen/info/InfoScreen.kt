package io.github.chrislo27.rhre3.screen.info

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.VersionHistory
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.credits.CreditsGame
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.editor.CameraBehaviour
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.*
import io.github.chrislo27.rhre3.sfxdb.GameMetadata
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.soundsystem.SoundCache
import io.github.chrislo27.rhre3.soundsystem.SoundStretch
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.rhre3.stage.bg.Background
import io.github.chrislo27.rhre3.util.FadeIn
import io.github.chrislo27.rhre3.util.FadeOut
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.isAltDown
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import io.github.chrislo27.toolboks.version.Version


class InfoScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, InfoScreen>(main), HidesVersionText {

    companion object {
        const val DEFAULT_AUTOSAVE_TIME = 5
        val autosaveTimers = listOf(0, 1, 2, 3, 4, 5, 10, 15)
        var shouldSeePartners: Boolean = true
            private set
    }

    enum class Page(val heading: String) {
        INFO("screen.info.info"), SETTINGS("screen.info.settings"), EXTRAS("screen.info.extras");

        companion object {
            val VALUES = values().toList()
        }
    }

    val preferences: Preferences
        get() = main.preferences
    val editor: Editor
        get() = ScreenRegistry.getNonNullAsType<EditorScreen>("editor").editor

    private var backgroundOnly = false
    private var currentPage: Page = Page.SETTINGS
        set(value) {
            field = value
            pageStages.forEach { it.visible = false }
            when (value) {
                Page.INFO -> {
                    infoStage.visible = true
                }
                Page.SETTINGS -> {
                    settingsStage.visible = true
                }
                Page.EXTRAS -> {
                    extrasStage.visible = true
                }
            }
            headingLabel.text = value.heading
            leftPageButton.visible = false
            rightPageButton.visible = false
            val index = Page.VALUES.indexOf(value)
            if (index > 0) {
                leftPageButton.run {
                    visible = true
                    targetPage = Page.VALUES[index - 1]
                    label.text = targetPage.heading
                }
            }
            if (index < Page.VALUES.size - 1) {
                rightPageButton.run {
                    visible = true
                    targetPage = Page.VALUES[index + 1]
                    label.text = targetPage.heading
                }
            }
        }
    override val hidesVersionText: Boolean
        get() = currentPage == Page.INFO
    override val stage: GenericStage<InfoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val settingsStage: SettingsStage
    private val infoStage: InfoStage
    private val extrasStage: Stage<InfoScreen>
    
    private val pageStages: List<Stage<InfoScreen>>
    private val leftPageButton: PageChangeButton
    private val rightPageButton: PageChangeButton
    private val headingLabel: TextLabel<InfoScreen>
    private val onlineLabel: TextLabel<InfoScreen>
    private val menuBgButton: Button<InfoScreen>

    init {
        val palette = stage.palette

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_info"))
        }
        stage.titleLabel.apply {
            this.text = "editor.info"
        }
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        stage.bottomStage.elements += object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.GITHUB)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                fun updateText() {
                    this.text = Localization["screen.info.github", RHRE3.GITHUB]
                }

                this.isLocalizationKey = false
                this.textWrapping = false
                updateText()
                this.fontScaleMultiplier = 0.9f

                Localization.addListener { updateText() }
            })

            this.location.set(screenX = 0.175f, screenWidth = 0.65f)
        }

        menuBgButton = object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            val numberLabel = TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.textAlign = Align.center
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 1f
                this.textWrapping = false
                this.location.set(screenX = 0.5f - 0.03f, screenWidth = 0.5f + 0.03f, screenY = 0.3f, screenHeight = 0.7f, pixelWidth = -1f)
            }
            val nameLabel = TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.textAlign = Align.center
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 0.6f
                this.textWrapping = false
                this.location.set(screenY = 0.05f, screenHeight = 0.25f, pixelX = 1f, pixelWidth = -2f)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
                hoverTime = 0f
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
                hoverTime = 0f
            }

            fun cycle(dir: Int) {
                val values = Background.backgrounds
                if (dir > 0) {
                    val index = values.indexOf(GenericStage.backgroundImpl) + 1
                    GenericStage.backgroundImpl = if (index >= values.size) {
                        values.first()
                    } else {
                        values[index]
                    }
                } else if (dir < 0) {
                    val index = values.indexOf(GenericStage.backgroundImpl) - 1
                    GenericStage.backgroundImpl = if (index < 0) {
                        values.last()
                    } else {
                        values[index]
                    }
                }

                numberLabel.text = "${values.indexOf(GenericStage.backgroundImpl) + 1}/${values.size}"
                nameLabel.text = "${Background.backgroundMapByBg[GenericStage.backgroundImpl]?.name}"

                main.preferences.putString(PreferenceKeys.BACKGROUND, GenericStage.backgroundImpl.id).flush()
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_palette"))
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.location.set(screenY = 0.3f, screenHeight = 0.7f, screenWidth = 0.5f)
            })
            this.addLabel(numberLabel)
            this.addLabel(nameLabel)

            this.cycle(0)

            this.location.set(screenX = 0.85f, screenWidth = 1f - 0.85f)
        }
        stage.bottomStage.elements += menuBgButton

        onlineLabel = object : TextLabel<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            var last = Int.MIN_VALUE

            init {
                Localization.addListener {
                    last = Int.MIN_VALUE
                }
            }

            override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                val current = main.liveUsers
                if (last != current) {
                    last = current
                    this.text = if (current > 0) Localization["screen.info.online", current] else ""
                }
                super.render(screen, batch, shapeRenderer)
            }
        }.apply {
            this.isLocalizationKey = false
            this.textAlign = Align.right
            this.fontScaleMultiplier = 0.5f
            this.alignment = Align.bottomRight
            this.location.set(screenHeight = 1f / 3,
                              screenWidth = this.stage.percentageOfWidth(this.stage.location.realHeight))
            this.location.set(screenX = this.location.screenWidth + 0.025f * 1.25f, screenY = -0.75f + 1f / 3)
        }
        stage.bottomStage.elements += onlineLabel
        stage.bottomStage.elements += object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override var visible: Boolean = true
                get() = field && main.liveUsers > 0 && !RHRE3.noOnlineCounter

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (!visible) return
                main.screen = OnlineCounterScreen(main, onlineLabel.text)
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_history"))
            })
            this.alignment = Align.bottomRight
            this.location.set(screenHeight = onlineLabel.location.screenHeight, screenY = onlineLabel.location.screenY)
            this.location.set(screenX = 0.025f, screenWidth = 0.025f)
        }

        infoStage = InfoStage(stage.centreStage, stage.camera, this)
        stage.centreStage.elements += infoStage
        settingsStage = SettingsStage(stage.centreStage, stage.camera, this)
        stage.centreStage.elements += settingsStage
        extrasStage = Stage(stage.centreStage, stage.camera)
        stage.centreStage.elements += extrasStage

        val padding = 0.025f
        val buttonHeight = 0.1f
        val fontScale = 0.75f
        stage.centreStage.also { centre ->
            val buttonWidth = 0.35f
            headingLabel = TextLabel(palette, centre, centre).apply {
                val width = 1f - (buttonWidth * 1.85f)
                this.location.set(screenX = 0.5f - width / 2f,
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = width,
                                  screenHeight = buttonHeight)
                this.isLocalizationKey = true
                this.text = "screen.info.settings"
            }
            centre.elements += headingLabel

            leftPageButton = PageChangeButton(palette, centre, centre, false).apply {
                this.location.set(0f, 1f - (padding + buttonHeight * 0.8f), buttonWidth * 0.75f, buttonHeight)
            }
            centre.elements += leftPageButton
            rightPageButton = PageChangeButton(palette, centre, centre, true).apply {
                this.location.set(1f - (buttonWidth * 0.75f), 1f - (padding + buttonHeight * 0.8f), buttonWidth * 0.75f, buttonHeight)
            }
            centre.elements += rightPageButton
        }
        
        extrasStage.also { extras ->
        }

        pageStages = listOf(infoStage, settingsStage, extrasStage)
        stage.updatePositions()
        currentPage = currentPage // force update
        updateSeePartners()
    }

    private fun updateSeePartners() {
        shouldSeePartners = main.preferences.getInteger(PreferenceKeys.VIEWED_PARTNERS_VERSION, 0) < PartnersScreen.PARTNERS_VERSION
    }

    override fun render(delta: Float) {
        super.render(delta)
        if (backgroundOnly || menuBgButton.hoverTime >= 1.5f) {
            val batch = main.batch
            batch.begin()
            GenericStage.backgroundImpl.render(main.defaultCamera, batch, main.shapeRenderer, 0f)
            batch.end()
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        } else if (Gdx.input.isControlDown() && !Gdx.input.isShiftDown() && !Gdx.input.isAltDown() && Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            main.screen = ScreenRegistry.getNonNull("advancedOptions")
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && Gdx.input.isKeyPressed(Toolboks.DEBUG_KEY)) {
            backgroundOnly = !backgroundOnly
        }
    }

    override fun show() {
        super.show()
        infoStage.show()
        DiscordHelper.updatePresence(PresenceState.InSettings)
        updateSeePartners()
    }

    override fun hide() {
        super.hide()

        // Analytics
        if (settingsStage.didChangeSettings) {
            val map: Map<String, *> = preferences.get()
            AnalyticsHandler.track("Exit Info and Settings",
                                   mapOf(
                                           "settings" to PreferenceKeys.allSettingsKeys.associate {
                                               it.replace("settings_", "") to (map[it] ?: "null")
                                           } + ("background" to map[PreferenceKeys.BACKGROUND])
                                        ))
        }

        settingsStage.didChangeSettings = false
    }

    override fun getDebugString(): String? {
        return "CTRL+A - Open Advanced Options\n${Input.Keys.toString(Toolboks.DEBUG_KEY)}+Q - Render background on top"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    inner class PageChangeButton(palette: UIPalette, parent: UIElement<InfoScreen>, stage: Stage<InfoScreen>, right: Boolean)
        : Button<InfoScreen>(palette, parent, stage) {

        var targetPage: Page = Page.INFO
        val label: TextLabel<InfoScreen> = TextLabel(palette, this, this.stage).apply {
            this.location.set(screenX = 0.15f, screenWidth = 0.85f)
            this.isLocalizationKey = true
            this.textAlign = if (right) Align.right else Align.left
            this.fontScaleMultiplier = 0.75f
            this.text = "screen.info.settings"
        }

        init {
            addLabel(label)
            if (right) {
                label.location.screenX = 0f
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.location.set(screenX = 0.85f, screenWidth = 0.15f)
                    this.isLocalizationKey = false
                    this.text = "\uE14A"
                })
            } else {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.location.set(screenX = 0f, screenWidth = 0.15f)
                    this.isLocalizationKey = false
                    this.text = "\uE149"
                })
            }

            leftClickAction = { _, _ ->
                currentPage = targetPage
            }
        }
    }
}