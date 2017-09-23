package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class InfoScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, InfoScreen>(main), HidesVersionText {

    companion object {

        const val DEFAULT_AUTOSAVE_TIME = 5
        val timers = listOf(0, 1, 2, 3, 4, 5, 10, 15)

    }

    override val stage: Stage<InfoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val preferences: Preferences
        get() = main.preferences
    private val editor: Editor
        get() = ScreenRegistry.getNonNullAsType<EditorScreen>("editor").editor

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

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        stage.centreStage.also { centre ->
            val padding = 0.025f
            val buttonWidth = 0.4f
            val buttonHeight = 0.1f
            val fontScale = 0.75f

            // left
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = padding,
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = true
                this.text = "screen.info.settings"
            }

            // right
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = 1f - (padding + buttonHeight * 0.8f),
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = true
                this.text = "screen.info.info"
            }
            centre.elements += TextLabel(palette, centre, centre).apply {
                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = 1f - (padding + buttonHeight * 0.8f) * 2,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 0.8f)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = RHRE3.VERSION.toString()
            }

            // info buttons
            // Credits
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    main.screen = ScreenRegistry.getNonNull("credits")
                }
            }.apply {

                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.credits"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // Editor version
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    main.screen = ScreenRegistry.getNonNull("editorVersion")
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.version"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // Database version
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    Gdx.net.openURI(RHRE3.DATABASE_RELEASES)
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.database"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 3 + buttonHeight * 2,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
            // RHRM
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    Gdx.net.openURI("https://github.com/inkedsplat/RHRM")
                }
            }.apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.rhrm"
                })

                this.location.set(screenX = 1f - (padding + buttonWidth),
                                  screenY = padding * 4 + buttonHeight * 3,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight * 2 + padding)
            }

            // Settings
            // Autosave timer
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                private fun updateText() {
                    textLabel.text = Localization["screen.info.autosaveTimer",
                            if (timers[index] == 0) Localization["screen.info.autosaveTimerOff"]
                            else Localization["screen.info.autosaveTimerMin", timers[index]]]
                    editor.resetAutosaveTimer()
                }

                private fun persist() {
                    preferences.putInteger(PreferenceKeys.SETTINGS_AUTOSAVE, timers[index]).flush()
                }

                private var index: Int = run {
                    val default = DEFAULT_AUTOSAVE_TIME
                    val pref = preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE, default)
                    timers.indexOf(timers.find { it == pref } ?: default).coerceIn(0, timers.size - 1)
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
                    if (index >= timers.size)
                        index = 0

                    persist()
                    updateText()
                }

                override fun onRightClick(xPercent: Float, yPercent: Float) {
                    super.onRightClick(xPercent, yPercent)
                    index--
                    if (index < 0)
                        index = timers.size - 1

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
                    this.fontScaleMultiplier = fontScale
                })

                this.location.set(screenX = padding,
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Chase camera
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.chaseCamera"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Disable minimap
            centre.elements += object : FalseCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_MINIMAP, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.disableMinimap"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 3 + buttonHeight * 2,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Subtitle order
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_SUBTITLE_ORDER, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_SUBTITLE_ORDER, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale * fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.subtitleOrder"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 4 + buttonHeight * 3,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // End remix at end
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.endAtLastCue"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 5 + buttonHeight * 4,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Smooth dragging
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, true)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.smoothDragging"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 6 + buttonHeight * 5,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Sound system
            centre.elements += object : Button<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    main.screen = ScreenRegistry.getNonNull("soundSystem")
                }
            }.apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = true
                    this.text = "screen.info.soundSystem"
                    this.fontScaleMultiplier = fontScale
                })

                this.location.set(screenX = padding,
                                  screenY = padding * 7 + buttonHeight * 6,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
        }

        stage.updatePositions()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        stage as GenericStage
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}