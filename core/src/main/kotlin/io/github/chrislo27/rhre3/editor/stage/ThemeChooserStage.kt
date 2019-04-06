package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Themes
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import kotlin.math.roundToInt


class ThemeChooserStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val buttons: List<ThemeChangeButton> = mutableListOf()
    private val preferences: Preferences
        get() = editor.main.preferences

    private var buttonScroll = 0
    private val maxButtonScroll: Int
        get() = (LoadedThemes.themes.size - buttons.size).coerceAtLeast(0)

    init {
        buttons as MutableList

        this.elements += object : ColourPane<EditorScreen>(this, this) {
            override fun scrolled(amount: Int): Boolean {
                scroll(-amount)
                return true
            }
        }.apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.colour.a = 0.8f
        }

        this.elements += TextLabel(palette, this, this).apply {
            this.location.set(screenX = 0f, screenWidth = 1f, screenY = 0.875f, screenHeight = 0.125f)

            this.textAlign = Align.center
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "editor.themeChooser.title"
            this.location.set(screenWidth = 0.95f, screenX = 0.025f)
        }

        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                LoadedThemes.reloadThemes(preferences, false)
                LoadedThemes.persistIndex(preferences)
                editor.theme = LoadedThemes.currentTheme
                buttonScroll = 0

                resetButtons()
            }
        }.apply {
            this.location.set(0.05f, 0.025f, 0.775f, 0.075f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "editor.themeChooser.reset"
                this.textWrapping = true
                this.fontScaleMultiplier = 0.75f
                this.location.set(pixelWidth = -4f, pixelX = 2f)
            })
        }

        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                Gdx.net.openURI("file:///${LoadedThemes.THEMES_FOLDER.file().absolutePath}")
            }
        }.apply {
            this.location.set(0.85f, 0.025f, 0.1f, 0.075f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
            })
        }

        val up = object : Button<EditorScreen>(palette, this, this) {
            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                (labels.first() as TextLabel).text = Editor.ARROWS[if (buttonScroll <= 0) 2 else 0]
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                scroll(-1)
            }
        }.apply {
            this.location.set(0.05f, 0.825f, 0.9f, 0.05f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = true
            })
        }
        this.elements += up
        val down = object : Button<EditorScreen>(palette, this, this) {
            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                (labels.first() as TextLabel).text = Editor.ARROWS[if (buttonScroll >= maxButtonScroll) 3 else 1]
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                scroll(1)
            }
        }.apply {
            this.location.set(0.05f, 0.1125f, 0.9f, 0.05f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = true
            })
        }
        this.elements += down

        val padding = 0.0075f
        val start = down.location.screenY + down.location.screenHeight + padding
        val end = up.location.screenY
        val area = end - start
        val numberOfButtons = 9
        val buttonHeight = area / numberOfButtons
        for (i in 0 until numberOfButtons) {
            buttons += ThemeChangeButton(i, palette, this, this).apply {
                this.location.set(screenX = 0.05f,
                                  screenY = end - (buttonHeight) * (i + 1),
                                  screenWidth = 0.9f,
                                  screenHeight = buttonHeight - padding)
            }
        }

        this.elements.addAll(buttons)

        // Scrollbar
        this.elements += object : UIElement<EditorScreen>(this, this) {
            private var clickPoint: Pair<Float, Int> = -1f to 1

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                val oldColour = batch.packedColor

                batch.packedColor = palette.backColor.toFloatBits()
                batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

                if (canBeClickedOn()) {
                    // Render window
                    val themes = LoadedThemes.themes
                    val total = themes.size
                    val barHeight = (buttons.size.toFloat() / total.coerceAtLeast(1)) * location.realHeight
                    val barHeightAlpha = barHeight / location.realHeight
                    val click = clickPoint

                    batch.color = if (click.first in 0f..1f) palette.clickedBackColor else palette.highlightedBackColor
                    batch.fillRect(location.realX, location.realY + location.realHeight - buttonScroll.toFloat() / total * location.realHeight, location.realWidth, -barHeight)

                    val numDefaultThemes = Themes.defaultThemes.size
                    if (total > numDefaultThemes) {
                        // Has custom, add blue bar
                        batch.setColor(0f, 1f, 1f, 0.5f)
                        val smallBarHeight = location.realHeight * (1f / total)
                        batch.fillRect(location.realX, location.realY + (1f - numDefaultThemes.toFloat() / total) * location.realHeight, location.realWidth, -smallBarHeight)
                    }

                    if (click.first in 0f..1f) {
                        val currentAlpha = 1f - (this.stage.camera.getInputY() - location.realY) / location.realHeight
                        val buttonScrollAlpha = click.second.toFloat() / total
                        val alpha = ((currentAlpha - (click.first - buttonScrollAlpha)) / (1f - barHeightAlpha)).coerceIn(0f, 1f)
                        val targetScroll = (alpha * maxButtonScroll).roundToInt().coerceIn(0, maxButtonScroll)
                        if (buttonScroll != targetScroll) {
                            buttonScroll = targetScroll
                            resetButtons()
                        }
                    }
                }

                batch.packedColor = oldColour
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (canBeClickedOn() && button == Input.Buttons.LEFT && isMouseOver()) {
                    val scrollAlpha = 1f - (this.stage.camera.getInputY() - location.realY) / location.realHeight
                    val total = LoadedThemes.themes.size
                    val alphaInBar = (scrollAlpha - (buttonScroll.toFloat() / total)) / (buttons.size.toFloat() / total.coerceAtLeast(1))
                    if (alphaInBar in 0f..1f) {
                        clickPoint = scrollAlpha to buttonScroll
                    }
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (canBeClickedOn() && button == Input.Buttons.LEFT) {
                    clickPoint = -1f to 1
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }

            override fun canBeClickedOn(): Boolean = maxButtonScroll > 0
        }.apply {
            this.location.set(screenX = 0.95f + 0.0075f, screenY = start, screenWidth = 0.035f, screenHeight = end - start - padding)
        }
    }

    fun resetButtons() {
        buttons.forEach(ThemeChangeButton::update)
    }

    private fun scroll(amount: Int) {
        if (amount < 0 && buttonScroll > 0) {
            buttonScroll--
        } else if (amount > 0 && buttonScroll < maxButtonScroll) {
            buttonScroll++
        }

        resetButtons()
    }

    override fun scrolled(amount: Int): Boolean {
        if (this.isMouseOver()) {
            scroll(amount)
        }

        return true
    }

    inner class ThemeChangeButton(val index: Int, palette: UIPalette, parent: UIElement<EditorScreen>,
                                  stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage) {

        private val textLabel: TextLabel<EditorScreen> = TextLabel(palette, this, stage)
        private val colourIcon: Label<EditorScreen> = object : Label<EditorScreen>(palette, this, stage) {
            override var background: Boolean = false

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (index + buttonScroll >= LoadedThemes.themes.size)
                    return

                val theme = LoadedThemes.themes[index + buttonScroll]
                val oldBatchColor = batch.packedColor

                batch.color = theme.background
                batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
                batch.color = theme.trackLine
                val lines = 4
                for (i in 0 until lines) {
                    batch.fillRect(location.realX,
                                   location.realY + location.realHeight * 1f / 3f
                                           + (location.realHeight / 3f * (i.toFloat() / (lines))),
                                   location.realWidth, 1f)
                }

                batch.packedColor = oldBatchColor
            }
        }

        init {
            val part = 1 / 6f

            textLabel.apply {
                this.location.set(screenX = part)
                this.location.set(screenWidth = 1f - this.location.screenX - 0.005f)
                this.textWrapping = false
                this.textAlign = Align.left
                this.fontScaleMultiplier = 0.7f
            }

            colourIcon.apply {
                val padding = 1f / 8f
                this.location.set(screenX = padding * part, screenWidth = part - padding * part * 2,
                                  screenY = padding, screenHeight = 1f - padding * 2)
            }

            addLabel(textLabel)
            addLabel(colourIcon)
        }

        fun update() {
            if (index + buttonScroll >= LoadedThemes.themes.size)
                return

            val theme = LoadedThemes.themes[index + buttonScroll]
            textLabel.text = theme.name
            textLabel.isLocalizationKey = theme.nameIsLocalization
            textLabel.textColor = if (index + buttonScroll == LoadedThemes.index) Editor.SELECTED_TINT else null
        }

        override fun frameUpdate(screen: EditorScreen) {
            this.visible = index + buttonScroll < LoadedThemes.themes.size

            super.frameUpdate(screen)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)

            LoadedThemes.index = index + buttonScroll
            LoadedThemes.persistIndex(preferences)
            editor.theme = LoadedThemes.currentTheme

            resetButtons()
        }
    }

}