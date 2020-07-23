package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.modding.ModdingGame
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
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
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import java.util.*
import kotlin.math.sign
import kotlin.system.measureNanoTime


class AdvancedOptionsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, AdvancedOptionsScreen>(main) {

    override val stage: GenericStage<AdvancedOptionsScreen>
    private val preferences: Preferences
        get() = main.preferences
    private var didChangeSettings: Boolean = false

    private val moddingGameLabel: TextLabel<AdvancedOptionsScreen>
    private val moddingGameWarningLabel: TextLabel<AdvancedOptionsScreen>
    private var seconds = 0f

    private val reloadMetadataButton: Button<AdvancedOptionsScreen>
    private val pitchStyleButton: Button<AdvancedOptionsScreen>
    private val explodingEntitiesButton: Button<AdvancedOptionsScreen>

    init {
        val palette = main.uiPalette
        stage = GenericStage(main.uiPalette, null, main.defaultCamera)

        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_adv_opts"))
        stage.titleLabel.isLocalizationKey = false
        stage.titleLabel.text = "Advanced Options"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        val bottom = stage.bottomStage
        // Advanced Options setting
        bottom.elements += TrueCheckbox(palette, bottom, bottom).apply {
            this.checked = main.settings.advancedOptions

            this.textLabel.apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "Other Advanced Options Enabled"
            }
            this.leftClickAction = { _, _ ->
                main.settings.advancedOptions = checked
                didChangeSettings = true
                main.settings.persist()
            }
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        val centre = stage.centreStage
        val padding = 0.025f
        val buttonWidth = 0.4f
        val buttonHeight = 0.1f
        val fontScale = 0.75f

        moddingGameLabel = TextLabel(palette, centre, centre).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.top or Align.center
            this.location.set(screenX = padding,
                              screenY = padding,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 3 + padding * 2)
        }
        centre.elements += moddingGameLabel
        moddingGameWarningLabel = TextLabel(palette, centre, centre).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.top or Align.center
            this.location.set(screenX = padding,
                              screenY = padding * 4 + buttonHeight * 3,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 2 + padding)
        }
        centre.elements += moddingGameWarningLabel
        // Modding game reference
        centre.elements += object : Button<AdvancedOptionsScreen>(palette, centre, centre) {
            private fun updateText() {
                val game = ModdingUtils.currentGame
                val underdeveloped = game.underdeveloped
                textLabel.text = "[LIGHT_GRAY]Modding utilities with reference to:[]\n${if (underdeveloped) "[ORANGE]" else ""}${game.fullName}${if (underdeveloped) "[]" else ""}"
                updateLabels()
            }

            private fun persist() {
                ModdingUtils.currentGame = ModdingGame.VALUES[index]
                preferences.putString(PreferenceKeys.ADVOPT_REF_RH_GAME, ModdingUtils.currentGame.id).flush()
                didChangeSettings = true
            }

            private var index: Int = run {
                val default = ModdingGame.DEFAULT_GAME
                val pref = preferences.getString(PreferenceKeys.ADVOPT_REF_RH_GAME, default.id)
                val values = ModdingGame.VALUES
                values.indexOf(values.find { it.id == pref } ?: default).coerceIn(0, values.size - 1)
            }

            private val textLabel: TextLabel<AdvancedOptionsScreen>
                get() = labels.first() as TextLabel

            override fun render(screen: AdvancedOptionsScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (textLabel.text.isEmpty()) {
                    updateText()
                }
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                index++
                if (index >= ModdingGame.VALUES.size)
                    index = 0

                persist()
                updateText()
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                index--
                if (index < 0)
                    index = ModdingGame.VALUES.size - 1

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
                this.fontScaleMultiplier = 0.8f
            })

            this.location.set(screenX = padding,
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 2 + padding)
        }


        // Reload modding metadata
        reloadMetadataButton = object : Button<AdvancedOptionsScreen>(palette, centre, centre) {
            private val textLabel: TextLabel<AdvancedOptionsScreen>
                get() = labels.first() as TextLabel

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                var success: Boolean = false
                val nano = measureNanoTime {
                    success = try {
                        SFXDatabase.data.loadModdingMetadata(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                    if (success) {
                        resetReloadMetadataButton()
                        textLabel.text = "[GREEN]Reloaded metadata successfully![]"
                    } else {
                        resetReloadMetadataButton()
                        textLabel.text = "[RED]Failed to reload modding metadata[]\n[LIGHT_GRAY]Check console for details[]"
                        textLabel.fontScaleMultiplier = 0.6f
                    }
                }
                Toolboks.LOGGER.info("Reloaded modding metadata ${if (!success) "un" else ""}successfully in ${nano / 1_000_000.0} ms")
            }

        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = "Reload modding metadata"
                this.textWrapping = false
                this.fontScaleMultiplier = 0.8f
            })

            this.location.set(screenX = padding,
                              screenY = padding * 8 + buttonHeight * 7,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        centre.elements += reloadMetadataButton
        // Open containing folder for modding metadata
        centre.elements += Button(palette, centre, centre).apply {
            val width = buttonWidth * 0.09f
            this.location.set(screenX = padding * 0.5f - width,
                              screenY = padding * 8 + buttonHeight * 7,
                              screenWidth = width,
                              screenHeight = buttonHeight)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
            })
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("file:///${SFXDatabase.CUSTOM_MODDING_METADATA_FOLDER.file().absolutePath}")
            }
        }

        // Semitone major/minor
        pitchStyleButton = object : Button<AdvancedOptionsScreen>(palette, centre, centre) {
            private val textLabel: TextLabel<AdvancedOptionsScreen>
                get() = labels.first() as TextLabel

            private fun cycle(dir: Int) {
                val values = Semitones.PitchStyle.VALUES
                val index = values.indexOf(Semitones.pitchStyle).coerceAtLeast(0)
                val absNextIndex = index + sign(dir.toFloat()).toInt()
                val nextIndex = if (absNextIndex < 0) values.size - 1 else if (absNextIndex >= values.size) 0 else absNextIndex
                val next = values[nextIndex]
                Semitones.pitchStyle = next
                main.preferences.putString(PreferenceKeys.ADVOPT_PITCH_STYLE, next.name).flush()
                updateLabels()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = "Pitch note style: "
                this.textWrapping = false
                this.fontScaleMultiplier = 0.8f
            })

            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 8 + buttonHeight * 7,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        centre.elements += pitchStyleButton
        // Exploding entities
        explodingEntitiesButton = TrueCheckbox(palette, centre, centre).apply {
            this.leftClickAction = { _, _ ->
                main.settings.advExplodingEntities = this@apply.checked
                main.settings.persist()
            }
            this.textLabel.also {
                it.isLocalizationKey = false
                it.text = "Entities explode when deleted"
                it.textWrapping = false
                it.fontScaleMultiplier = 0.8f
                it.textAlign = Align.left
            }
            this.checked = main.settings.advExplodingEntities
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        centre.elements += explodingEntitiesButton
        centre.elements += TrueCheckbox(palette, centre, centre).apply {
            this.leftClickAction = { _, _ ->
                main.settings.advIgnorePitchRestrictions = this@apply.checked
                main.settings.persist()
            }
            this.textLabel.also {
                it.isLocalizationKey = false
                it.text = "Ignore entity pitching restrictions"
                it.textWrapping = false
                it.fontScaleMultiplier = 0.8f
                it.textAlign = Align.left
            }
            this.checked = main.settings.advIgnorePitchRestrictions
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
//        centre.elements += Button(palette, centre, centre).apply {
//            this.leftClickAction = { _, _ ->
//                val defaultCamera = main.defaultCamera
//                val oldDim = defaultCamera.viewportWidth to defaultCamera.viewportHeight
//                defaultCamera.setToOrtho(false, RHRE3.WIDTH.toFloat(), RHRE3.HEIGHT.toFloat())
//                defaultCamera.update()
//                ScreenRegistry["editor"]?.dispose()
//                ScreenRegistry += "editor" to EditorScreen(main)
//                defaultCamera.setToOrtho(false, oldDim.first, oldDim.second)
//                defaultCamera.update()
//                SFXDatabase.reset()
//                main.screen = SFXDBLoadingScreen(main) { ScreenRegistry["editor"] }
//            }
//            this.location.set(screenX = 1f - (padding + buttonWidth),
//                              screenY = padding,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//            this.addLabel(TextLabel(palette, this, this.stage).apply {
//                this.isLocalizationKey = false
//                this.text = "Reload SFX Database"
//                this.textWrapping = false
//                this.fontScaleMultiplier = 0.8f
//            })
//            tooltipTextIsLocalizationKey = false
//            tooltipText = "[ORANGE]WARNING[]: This will clear the editor and discard all unsaved changes.\nReloads the entire SFX database. May fail (and crash) if there are errors.\nThis will also reload modding metadata from scratch."
//        }

        centre.elements += object : UIElement<AdvancedOptionsScreen>(centre, centre) {
            private val percentX: Float
                get() = (stage.camera.getInputX() - location.realX) / location.realWidth
            private var beginDraw = false
            private var completed = false
            private var timeSinceBeginDraw = 0f

            private fun drawLine(batch: SpriteBatch, progress: Float) {
                val circle = AssetRegistry.get<Texture>("ui_circle")
                val diameter = this.location.realHeight * 3f
                val smallDia = this.location.realHeight
                batch.draw(circle, this.location.realX, this.location.realY + this.location.realHeight / 2f - diameter / 2f, diameter, diameter)
                batch.draw(circle, this.location.realX + (this.location.realWidth * progress) - smallDia / 2f, this.location.realY, smallDia, smallDia)
                batch.fillRect(this.location.realX + diameter / 2f, this.location.realY, (this.location.realWidth * progress - diameter / 2f), this.location.realHeight)
            }

            override fun render(screen: AdvancedOptionsScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                batch.setColor(1f, 1f, 1f, 1f)
                drawLine(batch, 1f)
                batch.color = Color.ORANGE
                if (beginDraw) {
                    drawLine(batch, (percentX).coerceIn(0.005f, 1f))
                    timeSinceBeginDraw += Gdx.graphics.deltaTime
                    if (timeSinceBeginDraw in 0f..0.75f) {
                        val circle = AssetRegistry.get<Texture>("ui_circle")
                        val a = (timeSinceBeginDraw / 0.75f).coerceIn(0f, 1f)
                        val fullDiameter = this.location.realHeight * 3f
                        val diameter = fullDiameter * (1f - a) * 3f
                        batch.setColor(1f, 1f, 1f, (1f - a) * 0.6f)
                        batch.draw(circle, this.location.realX + fullDiameter / 2f - diameter / 2f, this.location.realY + this.location.realHeight / 2f - diameter / 2f, diameter, diameter)
                        batch.setColor(1f, 1f, 1f, 1f)
                    }
                } else if (completed) {
                    drawLine(batch, 1f)
                }
                batch.setColor(1f, 1f, 1f, 1f)
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (visible) {
                    if (isMouseOver() && !beginDraw) {
                        if (percentX in 0f..0.019f) {
                            beginDraw = true
                            timeSinceBeginDraw = 0f
                            completed = false
                            return true
                        }
                    } else {
                        val wasDrawing = beginDraw
                        beginDraw = false
                        if ((stage.camera.getInputX() - location.realX) >= location.realWidth - 1f) {
                            completed = true
                            Gdx.app.postRunnable {
                                main.screen = TransitionScreen(main, this@AdvancedOptionsScreen, LinePuzzleEndScreen(main), FadeOut(2f, Color.BLACK), FadeIn(1f, Color.BLACK))
                                AssetRegistry.get<Sound>("etc_sfx_record").play()
                            }
                        }
                        return wasDrawing
                    }
                    return false
                }
                return false
            }
        }.apply {
            this.location.set(screenX = -0.02f,
                              screenY = -buttonHeight * 0.275f,
                              screenWidth = 1.02f,
                              screenHeight = buttonHeight * 0.2f)
        }

        updateLabels()
    }

    private fun updateLabels() {
        val game = ModdingUtils.currentGame
        moddingGameWarningLabel.text = "[LIGHT_GRAY]${if (game.underdeveloped)
            "[ORANGE]Warning:[] modding info for this game\nis very underdeveloped and may be\nextremely lacking in info or incorrect."
        else
            "[YELLOW]Caution:[] modding info for this game\nmay only be partially complete and\nsubject to change."}[]\n"
        moddingGameLabel.text = "1 ♩ (quarter note) = ${game.beatsToTickflowString(1f)}${if (game.tickflowUnitName.isEmpty()) " rest units" else ""}"

        (pitchStyleButton.labels.first() as TextLabel).text = "Pitch note style: [LIGHT_GRAY]${Semitones.pitchStyle.name.toLowerCase(Locale.ROOT).capitalize()} (ex: ${Semitones.pitchStyle.example})[]"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        seconds += Gdx.graphics.deltaTime * 2.5f
        stage.titleIcon.rotation = (MathUtils.sin(MathUtils.sin(seconds * 2)) + seconds * 0.4f) * -90f
    }

    override fun hide() {
        super.hide()

        // Analytics
        if (didChangeSettings) {
            preferences.flush()
            val map: Map<String, *> = preferences.get()
            AnalyticsHandler.track("Exit Advanced Options",
                                   mapOf(
                                           "settings" to PreferenceKeys.allAdvOptsKeys.associate {
                                               it.replace("advOpt_", "") to (map[it] ?: "null")
                                           } + ("advancedOptions" to map[PreferenceKeys.SETTINGS_ADVANCED_OPTIONS])
                                        ))
        }

        didChangeSettings = false
    }

    override fun show() {
        super.show()
        seconds = 0f
        resetReloadMetadataButton()
    }

    private fun resetReloadMetadataButton() {
        (reloadMetadataButton.labels.first() as TextLabel).let {
            it.text = "Reload modding metadata"
            it.fontScaleMultiplier = 0.8f
        }
    }
}

class LinePuzzleEndScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, LinePuzzleEndScreen>(main), HidesVersionText {

    override fun show() {
        super.show()
        Gdx.app.postRunnable {
            RemixRecovery.saveRemixInRecovery()
            Gdx.app.postRunnable {
                EditorScreen.enteredEditor = false
                Gdx.app.exit()
            }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
