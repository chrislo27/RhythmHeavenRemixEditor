package io.github.chrislo27.rhre3.screen.info

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.credits.CreditsGame
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.CreditsScreen
import io.github.chrislo27.rhre3.sfxdb.GameMetadata
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.util.FadeIn
import io.github.chrislo27.rhre3.util.FadeOut
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.isAltDown
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown


class InfoStage(parent: UIElement<InfoScreen>?, camera: OrthographicCamera, val infoScreen: InfoScreen)
    : Stage<InfoScreen>(parent, camera) {

    private val main: RHRE3Application get() = infoScreen.main
    private val preferences: Preferences get() = infoScreen.preferences
    private val editor: Editor get() = infoScreen.editor

    private val loadingIcon: LoadingIcon<InfoScreen>
    private val clearRecentsButton: Button<InfoScreen>
    private val dbVersionLabel: TextLabel<InfoScreen>
    private val versionLabel: TextLabel<InfoScreen>

    init {
        val palette = infoScreen.stage.palette
        val padding = 0.025f
        val buttonHeight = 0.1f
        val fontScale = 0.75f

        val info = this
        val buttonWidth = 0.4f
        // Loading icon for paddler
        loadingIcon = LoadingIcon(palette, info).apply {
            this.location.set(screenX = 1f - (padding + buttonWidth) - buttonWidth * 0.09f + buttonWidth,
                              screenY = 1f - (padding + buttonHeight * 0.8f) * 3,
                              screenWidth = buttonWidth * 0.09f,
                              screenHeight = buttonHeight * 0.8f)
            this.visible = true
            this.alpha = 0f
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }
        info.elements += loadingIcon
        // current program version
        versionLabel = object : TextLabel<InfoScreen>(palette, info, info) {
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
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = 1f - (padding + buttonHeight * 0.8f) * 2,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 0.8f)
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = RHRE3.VERSION.toString()
        }
        info.elements += versionLabel
        dbVersionLabel = object : TextLabel<InfoScreen>(palette, info, info) {
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
                              screenHeight = buttonHeight * 0.8f,
                              pixelX = 2f, pixelWidth = -4f)
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "SFXDB VERSION"
        }
        info.elements += dbVersionLabel
        info.elements += Button(palette, info, info).apply {
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = 1f - (padding + buttonHeight * 0.8f) * 3,
                              screenWidth = buttonWidth * 0.085f,
                              screenHeight = buttonHeight * 0.8f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
            })
            this.leftClickAction = { _, _ ->

                Gdx.net.openURI("file:///${SFXDatabase.CUSTOM_SFX_FOLDER.file().absolutePath}")
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "editor.customSfx.openFolder"
        }

        // Partners button
        info.elements += object : Button<InfoScreen>(palette, info, info) {
            override fun render(screen: InfoScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (labels.isNotEmpty()) {
                    val first = labels.first()
                    if (first is ImageLabel) {
                        if (InfoScreen.shouldSeePartners) {
                            first.tint.fromHsv(MathHelper.getSawtoothWave(1.5f) * 360f, 0.3f, 0.75f)
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
            this.leftClickAction = { _, _ ->
                main.screen = ScreenRegistry.getNonNull("partners")
            }
        }
        info.elements += TextLabel(palette, info, info).apply {
            this.location.set(screenX = 0.5f - (0.1f / 2),
                              screenY = buttonHeight * 2 + padding * 2.5f,
                              screenWidth = 0.1f,
                              screenHeight = buttonHeight)
            this.isLocalizationKey = true
            this.textWrapping = false
            this.text = "screen.partners.title"
        }

        // Donate button
        info.elements += Button(palette, info, info).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.DONATION_URL)
            }
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

        // info buttons
        // Credits
        info.elements += object : Button<InfoScreen>(palette, info, info) {
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
                    main.screen = TransitionScreen(main, infoScreen, credits, FadeOut(0.5f, Color.BLACK), FadeIn(0.75f, Color.BLACK))
                    AssetRegistry.get<Sound>("sfx_enter_game").play()
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
                val shiftDown = (Gdx.input.isShiftDown() && !Gdx.input.isControlDown() && !Gdx.input.isAltDown() && main.screen == infoScreen) || (main.screen is TransitionScreen<*> && lastShift)
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
        // Clear recent games
        clearRecentsButton = Button(palette, info, info).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.clearRecents"
            })
            this.leftClickAction = { _, _ ->
                editor.updateRecentsList(null)
                enabled = false
                GameMetadata.persist()
            }
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 2 + buttonHeight,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        info.elements += clearRecentsButton


        // Editor version screen
        info.elements += Button(palette, info, info).apply {
            this.leftClickAction = { _, _ ->
                main.screen = ScreenRegistry.getNonNull("editorVersion")
            }
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.version"
            })

            this.location.set(screenX = padding,
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        // Database version changelog
        info.elements += Button(palette, info, info).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.DATABASE_RELEASES)
            }
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.database"
            })

            this.location.set(screenX = padding,
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        info.elements += Button(palette, info, info).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.DOCS_URL)
            }
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.docs"
            })

            this.location.set(screenX = padding,
                              screenY = padding * 5 + buttonHeight * 4,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
    }

    fun show() {
        clearRecentsButton.enabled = GameMetadata.recents.isNotEmpty()
        dbVersionLabel.text = Localization["screen.info.databaseVersion", "v${SFXDatabase.data.version}"]
        versionLabel.text = Localization["screen.info.programVersion", RHRE3.VERSION.toString()]
    }

}