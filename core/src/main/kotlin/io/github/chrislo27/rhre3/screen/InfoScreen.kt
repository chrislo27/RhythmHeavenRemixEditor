package io.github.chrislo27.rhre3.screen

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
import io.github.chrislo27.rhre3.registry.GameMetadata
import io.github.chrislo27.rhre3.registry.GameRegistry
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
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.isAltDown
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import io.github.chrislo27.toolboks.util.gdxutils.setHSB
import io.github.chrislo27.toolboks.version.Version


class InfoScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, InfoScreen>(main), HidesVersionText {

    companion object {
        const val DEFAULT_AUTOSAVE_TIME = 5
        val autosaveTimers = listOf(0, 1, 2, 3, 4, 5, 10, 15)
        var shouldSeePartners: Boolean = true
            private set
    }

    override val stage: Stage<InfoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val preferences: Preferences
        get() = main.preferences
    private val editor: Editor
        get() = ScreenRegistry.getNonNullAsType<EditorScreen>("editor").editor
    private val clearRecentsButton: Button<InfoScreen>
    private val dbVersionLabel: TextLabel<InfoScreen>
    private val versionLabel: TextLabel<InfoScreen>
    private var didChangeSettings: Boolean = Version.fromStringOrNull(preferences.getString(PreferenceKeys.LAST_VERSION, ""))?.let {
        !it.isUnknown && (it < VersionHistory.ANALYTICS || it < VersionHistory.RE_ADD_STRETCHABLE_TEMPO)
    } ?: false
    private val onlineLabel: TextLabel<InfoScreen>
    private val loadingIcon: LoadingIcon<InfoScreen>

    private var backgroundOnly = false

    init {
        stage as GenericStage<InfoScreen>
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

                Localization.listeners += { updateText() }
            })

            this.location.set(screenX = 0.175f, screenWidth = 0.65f)
        }

        stage.bottomStage.elements += object : Button<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            val numberLabel = TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.textAlign = Align.center
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 1f
                this.textWrapping = false
                this.location.set(screenX = 0.5f, screenWidth = 0.5f, screenY = 0.3f, screenHeight = 0.7f)
            }
            val nameLabel = TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), this, this.stage).apply {
                this.textAlign = Align.center
                this.isLocalizationKey = false
                this.fontScaleMultiplier = 0.6f
                this.textWrapping = false
                this.location.set(screenY = 0.05f, screenHeight = 0.25f)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
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
                nameLabel.text = "${Background.backgroundsNames[GenericStage.backgroundImpl]}"

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

        onlineLabel = object : TextLabel<InfoScreen>(palette, stage.bottomStage, stage.bottomStage) {
            var last = Int.MIN_VALUE
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

        stage.centreStage.also { centre ->
            val padding = 0.025f
            val buttonWidth = 0.4f
            val buttonHeight = 0.1f
            val fontScale = 0.75f

            // left heading
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = padding,
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = true
                this.text = "screen.info.settings"
            }

            // right heading
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = true
                this.text = "screen.info.info"
            }
            // Loading icon for paddler
            loadingIcon = LoadingIcon(palette, centre).apply {
                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = 1f - (padding + buttonHeight * 0.8f) * 2,
                                  screenWidth = buttonWidth * 0.09f,
                                  screenHeight = buttonHeight * 0.8f)
                this.visible = true
                this.alpha = 0f
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            }
            centre.elements += loadingIcon
            // current program version
            versionLabel = object : TextLabel<InfoScreen>(palette, centre, centre) {
                private var clicks = 0
                private var timeSinceLastClick = System.currentTimeMillis()
                private val CLICKS_RESET = 3000L
                private val color = Color(1f, 1f, 1f, 1f)
                private val notes = listOf(0, 2, 4, 5, 7)

                init {
                    this.textColor = color
                }

                override fun canBeClickedOn(): Boolean = true

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    if (System.currentTimeMillis() - timeSinceLastClick >= CLICKS_RESET) {
                        clicks = 0
                    }
                    AssetRegistry.get<Sound>("weird_sfx_bts_c").play(0.5f, Semitones.getALPitch(notes.getOrElse(clicks) { 0 }), 0f)
                    clicks++
                    timeSinceLastClick = System.currentTimeMillis()

                    if (clicks >= 5) {
                        clicks = 0
                        main.screen = ScreenRegistry.getNonNull("advancedOptions")
                        AssetRegistry.get<Sound>("weird_sfx_bts_pew").play(0.5f)
                    }
                }

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    val alpha = ((1f - (System.currentTimeMillis() - timeSinceLastClick) / 1000f * 4f)).coerceIn(0f, 1f)
                    color.set(1f, 1f, 1f, 1f).lerp(0f, 1f, 1f, 1f, alpha)
                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {
                this.location.set(screenX = 1f - (padding + buttonWidth) + buttonWidth * 0.09f,
                                  screenY = 1f - (padding + buttonHeight * 0.8f) * 2,
                                  screenWidth = buttonWidth - buttonWidth * 0.09f * 2,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = RHRE3.VERSION.toString()
            }
            centre.elements += versionLabel
            dbVersionLabel = object : TextLabel<InfoScreen>(palette, centre, centre) {
                private var clicks = 0
                private var timeSinceLastClick = System.currentTimeMillis()
                private val color = Color(1f, 1f, 1f, 1f)
                private var resetTime = 0L

                init {
                    this.textColor = color
                }

                override fun canBeClickedOn(): Boolean = true

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    clicks++
                    timeSinceLastClick = System.currentTimeMillis()

                    AssetRegistry.get<Sound>("weird_sfx_honk").play(0.5f)
                    LoadingIcon.usePaddlerAnimation = !LoadingIcon.usePaddlerAnimation
                    main.preferences.putBoolean(PreferenceKeys.PADDLER_LOADING_ICON, LoadingIcon.usePaddlerAnimation).flush()
                    resetTime = System.currentTimeMillis() + 6000L
                }

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    val alpha = ((1f - (System.currentTimeMillis() - timeSinceLastClick) / 1000f * 4f)).coerceIn(0f, 1f)
                    color.set(1f, 1f, 1f, 1f).lerp(1f, 0f, 0f, 1f, alpha)

                    loadingIcon.alpha = ((resetTime - System.currentTimeMillis()) / 1000f * 2).coerceIn(0f, 1f)

                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {
                this.location.set(screenX = 1f - (padding + buttonWidth) + buttonWidth * 0.09f,
                                  screenY = 1f - (padding + buttonHeight * 0.8f) * 3,
                                  screenWidth = buttonWidth - buttonWidth * 0.09f * 2,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = "DB VERSION"
            }
            centre.elements += dbVersionLabel
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    Gdx.net.openURI("file:///${GameRegistry.CUSTOM_SFX_FOLDER.file().absolutePath}")
                }
            }.apply {
                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = 1f - (padding + buttonHeight * 0.8f) * 3,
                                  screenWidth = buttonWidth * 0.09f,
                                  screenHeight = buttonHeight * 0.8f)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
                })
            }

            // Partners button
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    main.screen = ScreenRegistry.getNonNull("partners")
                }

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    if (labels.isNotEmpty()) {
                        val first = labels.first()
                        if (first is ImageLabel) {
                            if (InfoScreen.shouldSeePartners) {
                                first.tint.setHSB(MathHelper.getSawtoothWave(1.5f), 0.3f, 0.75f)
                            } else {
                                first.tint.set(1f, 1f, 1f, 1f)
                            }
                        }
                    }
                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_credits"))
                })

                this.location.set(screenX = 0.5f - (0.1f / 2),
                                  screenY = padding,
                                  screenWidth = 0.1f,
                                  screenHeight = buttonHeight * 2 + padding)
            }
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = 0.5f - (0.1f / 2),
                                  screenY = buttonHeight * 2 + padding * 2.5f,
                                  screenWidth = 0.1f,
                                  screenHeight = buttonHeight)
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.partners.title"
            }

            // Donate button
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    Gdx.net.openURI(RHRE3.DONATION_URL)
                }
            }.apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_donate"))
                })

                this.location.set(screenX = 0.5f - (0.1f / 2),
                                  screenY = padding,
                                  screenWidth = 0.1f,
                                  screenHeight = buttonHeight)
                this.visible = false
            }
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    Gdx.net.openURI(RHRE3.DOCS_URL)
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.textWrapping = false
                    this.text = "screen.info.docs"
                    this.fontScaleMultiplier = 0.8f
                })

                this.location.set(screenX = 0.5f - (0.1f / 2),
                                  screenY = padding * 4 + buttonHeight * 3,
                                  screenWidth = 0.1f,
                                  screenHeight = buttonHeight * 2 + padding)
            }

            // info buttons
            // Credits
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {

                init {
                    addLabel(TextLabel(palette, this, this.stage).apply {
                        this.fontScaleMultiplier = fontScale
                        this.isLocalizationKey = true
                        this.textWrapping = false
                        this.text = "screen.info.credits"
                    })
                    addLabel(ImageLabel(palette, this, this.stage).apply {
                        this.location.set(screenX = 0f, screenWidth = 0.1f)
                        this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                        this.image = TextureRegion(AssetRegistry.get<Texture>("weird_wakaaa"))
                    })
                }

                private val textLabel: TextLabel<InfoScreen>
                    get() = labels.first { it is TextLabel } as TextLabel
                private val imageLabel: ImageLabel<InfoScreen>
                    get() = labels.first { it is ImageLabel } as ImageLabel

                private var lastShift = false
                private var spinStart: Long = System.currentTimeMillis()

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    try {
                        val credits = CreditsGame(main, if (Gdx.input.isShiftDown()) 1.25f else 1f)
                        main.screen = TransitionScreen(main, this@InfoScreen, credits, FadeOut(0.5f, Color.BLACK), FadeIn(0.75f, Color.BLACK))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        main.screen = CreditsScreen(main)
                    }
                }

                override fun onRightClick(xPercent: Float, yPercent: Float) {
                    super.onRightClick(xPercent, yPercent)

                    main.screen = CreditsScreen(main)
                }

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    val shiftDown = (Gdx.input.isShiftDown() && main.screen == this@InfoScreen) || (main.screen is TransitionScreen<*> && lastShift)
                    if (lastShift != shiftDown) {
                        lastShift = shiftDown
                        textLabel.textColor = if (shiftDown) {
                            Colors.get("RAINBOW")
                        } else {
                            null
                        }
                        if (shiftDown) {
                            textLabel.isLocalizationKey = true
                            textLabel.text = "playalong.tempoUp"
                            spinStart = System.currentTimeMillis()
                        } else {
                            textLabel.isLocalizationKey = true
                            textLabel.text = "screen.info.credits"
                            imageLabel.rotation = 0f
                        }
                    }
                    if (shiftDown) {
                        imageLabel.rotation = MathHelper.getSawtoothWave(System.currentTimeMillis() - spinStart, 1.5f) * -360f
                    }
                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // Editor version screen
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    main.screen = ScreenRegistry.getNonNull("editorVersion")
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.text = "screen.info.version"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // Database version changelog
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    Gdx.net.openURI(RHRE3.DATABASE_RELEASES)
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.text = "screen.info.database"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 3 + buttonHeight * 2,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // Clear recent games
            clearRecentsButton = object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    editor.updateRecentsList(null)
                    enabled = false
                    GameMetadata.persist()
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.text = "screen.info.clearRecents"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 4 + buttonHeight * 3,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            centre.elements += clearRecentsButton
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {

                override val checkLabelPortion: Float = 0.1f

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_DISCORD_RPC_ENABLED, checked).flush()
                    didChangeSettings = true
                    DiscordHelper.enabled = checked
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
                                  screenY = padding * 5 + buttonHeight * 4,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 2.25f, screenWidth = 1f - checkLabelPortion * 2.25f)

                addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.location.set(screenX = checkLabelPortion, screenWidth = checkLabelPortion)
                    this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_discord"))
                })
            }

            // Settings
            // Autosave timer
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                private fun updateText() {
                    textLabel.text = Localization["screen.info.autosaveTimer",
                            if (autosaveTimers[index] == 0) Localization["screen.info.autosaveTimerOff"]
                            else Localization["screen.info.autosaveTimerMin", autosaveTimers[index]]]
                    editor.resetAutosaveTimer()
                }

                private fun persist() {
                    preferences.putInteger(PreferenceKeys.SETTINGS_AUTOSAVE, autosaveTimers[index]).flush()
                    didChangeSettings = true
                }

                private var index: Int = run {
                    val default = DEFAULT_AUTOSAVE_TIME
                    val pref = preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE, default)
                    autosaveTimers.indexOf(autosaveTimers.find { it == pref } ?: default).coerceIn(0, autosaveTimers.size - 1)
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
                    if (index >= autosaveTimers.size)
                        index = 0

                    persist()
                    updateText()
                }

                override fun onRightClick(xPercent: Float, yPercent: Float) {
                    super.onRightClick(xPercent, yPercent)
                    index--
                    if (index < 0)
                        index = autosaveTimers.size - 1

                    persist()
                    updateText()
                }

                init {
                    Localization.listeners += {
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

                this.location.set(screenX = padding,
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Chase camera
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                private val label: TextLabel<InfoScreen> = TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                    this.textWrapping = false
                    this.fontScaleMultiplier = fontScale
                    this.location.set(pixelX = 2f, pixelWidth = -4f)
                    addLabel(this)
                }

                private fun updateText() {
                    label.text = Localization["screen.info.cameraBehaviour", Localization[Editor.cameraBehaviour.localizationKey]]
                }

                private fun cycle(dir: Int) {
                    val values = CameraBehaviour.VALUES
                    val index = values.indexOf(Editor.cameraBehaviour) + dir
                    val normalized = if (index < 0) values.size else if (index >= values.size) 0 else index
                    Editor.cameraBehaviour = values[normalized]
                    if (dir != 0) {
                        preferences.putString(PreferenceKeys.SETTINGS_CAMERA_BEHAVIOUR, Editor.cameraBehaviour.name).flush()
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
                    Localization.listeners += { updateText() }
                    updateText()
                }
            }.apply {
                this.location.set(screenX = padding,
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Disable minimap
            centre.elements += object : FalseCheckbox<InfoScreen>(palette, centre, centre) {
                override val checkLabelPortion: Float = 0.1f
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_MINIMAP, checked).flush()
                    didChangeSettings = true
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP, false)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.textAlign = Align.left
                    this.text = "screen.info.disableMinimap"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 4 + buttonHeight * 3,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Minimap preview
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {

                override val checkLabelPortion: Float = 0.1f
                private var bufferSupported = true

                override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    if (bufferSupported && !editor.stage.minimap.bufferSupported) {
                        bufferSupported = false
                        textLabel.text = "screen.info.minimapPreview.unsupported"
                        textLabel.fontScaleMultiplier = fontScale * fontScale
                        checked = false
                    }
                    enabled = bufferSupported && !preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP, false)

                    super.render(screen, batch, shapeRenderer)
                }

                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    if (bufferSupported) {
                        preferences.putBoolean(PreferenceKeys.SETTINGS_MINIMAP_PREVIEW, checked).flush()
                        didChangeSettings = true
                    }
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP_PREVIEW, true)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

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
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override val checkLabelPortion: Float = 0.1f
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_SUBTITLE_ORDER, checked).flush()
                    didChangeSettings = true
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_SUBTITLE_ORDER, false)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

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

            // Remix stops at last cue
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override val checkLabelPortion: Float = 0.1f
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, checked).flush()
                    didChangeSettings = true
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, false)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.textAlign = Align.left
                    this.text = "screen.info.stopAtLastCue"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 6 + buttonHeight * 5,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Smooth dragging
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override val checkLabelPortion: Float = 0.1f
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, checked).flush()
                    didChangeSettings = true
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, true)

                this.checkLabel.location.set(screenWidth = checkLabelPortion)
                this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    this.textAlign = Align.left
                    this.text = "screen.info.smoothDragging"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 7 + buttonHeight * 6,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

        }

        stage.updatePositions()

        updateSeePartners()
    }

    private fun updateSeePartners() {
        shouldSeePartners = main.preferences.getInteger(PreferenceKeys.VIEWED_PARTNERS_VERSION, 0) < PartnersScreen.PARTNERS_VERSION
    }

    override fun render(delta: Float) {
        super.render(delta)
        if (backgroundOnly) {
            val batch = main.batch
            batch.begin()
            GenericStage.backgroundImpl.render(main.defaultCamera, batch, main.shapeRenderer, 0f)
            batch.end()
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        stage as GenericStage
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
        clearRecentsButton.enabled = GameMetadata.recents.isNotEmpty()
        dbVersionLabel.text = Localization["screen.info.databaseVersion", "v${GameRegistry.data.version}"]
        versionLabel.text = RHRE3.VERSION.toString()
        DiscordHelper.updatePresence(PresenceState.InSettings)
        updateSeePartners()
    }

    override fun hide() {
        super.hide()

        // Analytics
        if (didChangeSettings) {
            val map: Map<String, *> = preferences.get()
            AnalyticsHandler.track("Exit Info and Settings",
                                   mapOf(
                                           "settings" to PreferenceKeys.allSettingsKeys.associate {
                                               it.replace("settings_", "") to (map[it] ?: "null")
                                           } + ("background" to map[PreferenceKeys.BACKGROUND])
                                        ))
        }

        didChangeSettings = false
    }

    override fun getDebugString(): String? {
        return "${Input.Keys.toString(Toolboks.DEBUG_KEY)}+Q - Render background on top"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}